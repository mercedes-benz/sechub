// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventData;
import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventType;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.job.PDSJob;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesMonitoringStatus;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemHandlesJobCancelRequests;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemSigTermHandling;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * This class is responsible for all execution queuing parts - it will also know
 * what currently is happening, which job is started, executed etc. But will
 * make no changes to database.<br>
 * <br>
 *
 * <u>Details:</u><br>
 * A defined thread pool is used for execution queuing, an overload of the queue
 * must be checked by callers via {@link #isQueueFull()}. Execution itself is
 * done inside {@link PDSExecutionCallable} - execution state checks and changes
 * to database are done inside {@link PDSExecutionWatcher}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class PDSExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionService.class);
    private static final int DEFAULT_WORKER_THREAD_COUNT = 5;
    private static final int DEFAULT_QUEUE_MAX = 50;
    ExecutorService workers;

    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final PDSExecutionWatcher watcher = new PDSExecutionWatcher();
    private final Map<UUID, Future<PDSExecutionResult>> jobsInQueue = new LinkedHashMap<>();

    @PDSMustBeDocumented(value = "Set amount of worker threads used for exeuctions", scope = "execution")
    @Value("${pds.config.execute.worker.thread.count:" + DEFAULT_WORKER_THREAD_COUNT + "}")
    int workerThreadCount = DEFAULT_WORKER_THREAD_COUNT;

    @PDSMustBeDocumented(value = "Set amount of maximum executed parts in queue for same time", scope = "execution")
    @Value("${pds.config.execute.queue.max:" + DEFAULT_QUEUE_MAX + "}")
    int queueMax = DEFAULT_QUEUE_MAX;

    /* only for tests to turn off watcher */
    boolean watcherDisabled;

    @Autowired
    PDSExecutionCallableFactory executionCallableFactory;

    @Autowired
    PDSJobTransactionService jobTransactionService;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSWorkspaceService workspaceService;

    @PostConstruct
    protected void postConstruct() {
        workers = Executors.newFixedThreadPool(workerThreadCount);

        scheduler.scheduleAtFixedRate(watcher, 300, 1000, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    @UseCaseSystemSigTermHandling(@PDSStep(number = 1, name = "Mark running jobs needing restart", description = "All running jobs in queue, which are not already done, will get the state READY_TO_RESTART"))
    protected void preDestroy() {
        /*
         * The field "scheduler" is a `java.util.concurrent.ScheduledExecutorService`
         * and not a spring component. Because of this, we listen here to the spring
         * boot destroy signal and shutdown the executor service by our own:
         */
        scheduler.shutdown(); // will stop processing new parts!

        LOG.info("Scheduler executor service shutdown done");

        Set<UUID> jobsToRestart = new LinkedHashSet<>();
        Iterator<Entry<UUID, Future<PDSExecutionResult>>> it = jobsInQueue.entrySet().iterator();
        while (it.hasNext()) {
            Entry<UUID, Future<PDSExecutionResult>> entry = it.next();
            Future<PDSExecutionResult> future = entry.getValue();
            if (!future.isDone()) {
                /* still running - must be restarted by next instance */
                UUID pdsJobUUID = entry.getKey();
                jobsToRestart.add(pdsJobUUID);
            }
        }
        LOG.info("Handling predestroy for {} jobs in queue.", jobsToRestart.size());
        jobTransactionService.forceStateResetInOwnTransaction(jobsToRestart, PDSJobStatusState.READY_TO_START);
    }

    /**
     * Tries to cancels given job
     *
     * @param jobUUID
     * @return {@link CancelResult}, never <code>null</code>
     */
    @UseCaseSystemHandlesJobCancelRequests(@PDSStep(name = "service call", description = "job execution will be canceled in queue", number = 3))
    public CancelResult cancel(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        LOG.debug("Try to cancel PDS job: {} if running at this cluster member", jobUUID);

        synchronized (jobsInQueue) {

            Iterator<Entry<UUID, Future<PDSExecutionResult>>> it = jobsInQueue.entrySet().iterator();
            while (it.hasNext()) {
                Entry<UUID, Future<PDSExecutionResult>> entry = it.next();
                if (jobUUID.equals(entry.getKey())) {
                    Future<PDSExecutionResult> future = entry.getValue();
                    if (future.isDone()) {
                        /* already done or canceled */
                        LOG.info("cancellation of job with uuid:{} skipped, because already done", jobUUID);
                        return CancelResult.JOB_FOUND_CANCEL_WAS_DONE;
                    }
                    LOG.debug("Found PDS job: {} running at this cluster member", jobUUID);

                    ExecutionEventData eventData = new ExecutionEventData();

                    workspaceService.sendEvent(jobUUID, ExecutionEventType.CANCEL_REQUESTED, eventData);

                    /*
                     * the next call will trigger PDSExecutionCallable to cancel which will use the
                     * event data for further inspections
                     */
                    boolean canceled = future.cancel(true);

                    if (canceled) {
                        LOG.info("Cancel SUCCESSFUL: canceled PDS job: {}", jobUUID);
                        jobTransactionService.markJobAsCanceledInOwnTransaction(jobUUID);
                        return CancelResult.JOB_FOUND_CANCEL_WAS_DONE;
                    } else {
                        LOG.info(
                                "Cancel FAILED: was not able to cancel PDS job :{} - should not happen. Please read logs for details. This will be an orphaned cancel request.",
                                jobUUID);
                        return CancelResult.JOB_FOUND_CANCEL_WAS_NOT_POSSIBLE;

                    }
                }
            }
            /*
             * job not found - either never existed or already canceled/done and removed by
             * watcher
             */
            return CancelResult.JOB_NOT_FOUND;
        }
    }

    public enum CancelResult {
        JOB_FOUND_CANCEL_WAS_DONE,

        JOB_FOUND_JOB_ALREADY_DONE,

        JOB_FOUND_CANCEL_WAS_NOT_POSSIBLE,

        JOB_NOT_FOUND,

    }

    public boolean isQueueFull() {
        synchronized (jobsInQueue) {
            return jobsInQueue.size() >= queueMax;
        }
    }

    @Async
    public void addToExecutionQueueAsynchron(UUID jobUUID) {
        Future<?> former = null;
        synchronized (jobsInQueue) {
            LOG.debug("add job to execution queue:{}", jobUUID);
            int size = jobsInQueue.size();
            if (size >= queueMax) {
                LOG.warn("execution queue overload:{}/{}", size, queueMax);
            }
            PDSExecutionFutureTask task = new PDSExecutionFutureTask(executionCallableFactory.createCallable(jobUUID));
            workers.execute(task);

            former = jobsInQueue.put(jobUUID, task);
        }
        handleFormerJob(jobUUID, former);
    }

    @UseCaseAdminFetchesMonitoringStatus(@PDSStep(name = "db lookup", description = "service fetches all execution state", number = 2))
    public PDSExecutionStatus getExecutionStatus() {
        PDSExecutionStatus status = new PDSExecutionStatus();
        synchronized (jobsInQueue) {
            status.queueMax = queueMax;
            status.jobsInQueue = jobsInQueue.size();

            Iterator<Entry<UUID, Future<PDSExecutionResult>>> it = jobsInQueue.entrySet().iterator();
            while (it.hasNext()) {
                Entry<UUID, Future<PDSExecutionResult>> entry = it.next();
                Future<PDSExecutionResult> future = entry.getValue();
                PDSExecutionJobInQueueStatusEntry statusEntry = new PDSExecutionJobInQueueStatusEntry();
                statusEntry.done = future.isDone();
                statusEntry.canceled = future.isCancelled();
                statusEntry.jobUUID = entry.getKey();

                Optional<PDSJob> jobOption = repository.findById(entry.getKey());
                if (jobOption.isPresent()) {
                    PDSJob job = jobOption.get();
                    statusEntry.created = job.getCreated();
                    statusEntry.started = job.getStarted();
                    statusEntry.state = job.getState();
                }
                status.entries.add(statusEntry);
            }
        }
        return status;
    }

    private void handleFormerJob(UUID jobUUID, Future<?> former) {
        if (former == null) {
            return;
        }
        LOG.error("Did found former job in queue with same job UUID:{}", jobUUID);
        former.cancel(true);
        LOG.info("Canceled former job {}", jobUUID);

    }

    private class PDSExecutionWatcher implements Runnable {

        private static final int MAXIMUM_TRIES_TO_STORE_JOB_RESILIENT = 5;

        @Override
        public void run() {
            if (watcherDisabled) {
                LOG.warn("Execution watcher disabled");
                return;
            }
            inspectJobsInQueue();
        }

        private void inspectJobsInQueue() {
            synchronized (jobsInQueue) {
                List<UUID> doneAndDatabaseChangesApplied = new ArrayList<>(0);
                Iterator<Entry<UUID, Future<PDSExecutionResult>>> it = jobsInQueue.entrySet().iterator();

                while (it.hasNext()) {

                    Entry<UUID, Future<PDSExecutionResult>> entry = it.next();
                    Future<PDSExecutionResult> future = entry.getValue();

                    if (future.isDone()) {
                        if (isFutureDoneAndChangesToDatabaseCanBeApplied(entry, future)) {
                            doneAndDatabaseChangesApplied.add(entry.getKey());
                        }
                    }
                }

                for (UUID uuid : doneAndDatabaseChangesApplied) {
                    jobsInQueue.remove(uuid);
                }
            }
        }

        /**
         * Handles work being done - all done parts are marked automatically in
         * database. The execution will be tried resilient. See
         * {@link #getMaximumRetriesToStoreResilient()}
         *
         * @param entry
         * @param future
         * @return <code>true</code> when work can be removed from jobsInQueue
         */
        @UseCaseSystemHandlesJobCancelRequests(@PDSStep(name = "queue work", description = "canceled job will be marked as CANCELED in db", number = 5))
        private boolean isFutureDoneAndChangesToDatabaseCanBeApplied(Entry<UUID, Future<PDSExecutionResult>> entry, Future<PDSExecutionResult> future) {
            UUID jobUUID = entry.getKey();

            int tries = 0;
            while (tries < getMaximumRetriesToStoreResilient()) {
                if (tries > 0) {
                    LOG.info("Retry to store work for PDS job {}. Tried {} times before");
                }
                tries++;
                Optional<PDSJob> jobOption = repository.findById(jobUUID);
                if (!jobOption.isPresent()) {
                    LOG.error("pds job with uuid:{} does no longer exist, but result available! So remove from queue", jobUUID);
                    return true;
                }

                try {
                    PDSJob job = jobOption.get();
                    // we use this moment of time for all, currently the easiest and central way
                    job.setEnded(LocalDateTime.now());

                    if (future.isCancelled()) {
                        job.setState(PDSJobStatusState.CANCELED);
                    } else {
                        PDSExecutionResult callResult;
                        try {
                            callResult = future.get();
                            LOG.debug("Fetch job result from future, pds job uuid={}, state={}", job.getUUID(), job.getState());
                            job.setResult(callResult.result);

                            if (callResult.canceled) {
                                job.setState(PDSJobStatusState.CANCELED);
                            } else if (callResult.failed) {
                                job.setState(PDSJobStatusState.FAILED);
                            } else {
                                job.setState(PDSJobStatusState.DONE);
                            }

                        } catch (InterruptedException e) {
                            LOG.error("Job with uuid:{} was interrupted", jobUUID, e);

                            job.setState(PDSJobStatusState.FAILED);
                            job.setResult("Job interrupted");
                        } catch (ExecutionException e) {
                            LOG.error("Job with uuid:{} failed in execution", jobUUID, e);

                            job.setState(PDSJobStatusState.FAILED);
                            job.setResult("Job execution failed");
                        }
                        LOG.debug("Handled job result and state job uuid={}, state={}", job.getUUID(), job.getState());
                    }
                    repository.save(job);
                    LOG.debug("Stored job pds uuid={}, state={}", job.getUUID(), job.getState());

                    return true;

                } catch (OptimisticLockingFailureException e) {
                    LOG.warn("Optimistic lock problem - so not able to handle work for job with uuid:{}", jobUUID, e);
                } catch (Exception e) {
                    LOG.error("Eror happend - so not able to handle work for job with uuid:{}", jobUUID, e);
                }
            }

            LOG.error("Was not able to write work for job with uuid:{} - even after {} tries.", jobUUID, tries);
            // now we return true, so the job will be removed from the queue!
            return true;

        }

        private int getMaximumRetriesToStoreResilient() {
            return MAXIMUM_TRIES_TO_STORE_JOB_RESILIENT;
        }

    }

}
