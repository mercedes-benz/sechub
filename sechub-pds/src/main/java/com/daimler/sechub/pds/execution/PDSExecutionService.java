package com.daimler.sechub.pds.execution;

import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;

/**
 * This class is responsible for all execution queuing parts - it will also know
 * what currently is happening, which job is started, executed etc. But will
 * make no changes to database
 * 
 * @author Albert Tregnaghi
 *
 */
@Service
public class PDSExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionService.class);
    private static final int DEFAULT_WORKER_THREAD_COUNT = 5;
    private static final int DEFAULT_QUEUE_MAX = 50;
    private Map<UUID, Future<PDSExecutionCallResult>> jobsInQueue = new LinkedHashMap<>();
    private ExecutorService workExecutorService;

    private ScheduledExecutorService scheduledCheckWorkExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Value("${sechub.pds.config.execute.worker.thread.count:" + DEFAULT_WORKER_THREAD_COUNT + "}")
    int workerThreadCount = DEFAULT_WORKER_THREAD_COUNT;

    @Value("${sechub.pds.config.execute.queue.max:" + DEFAULT_QUEUE_MAX + "}")
    int queueMax = DEFAULT_QUEUE_MAX;

    @Autowired
    PDSJobRepository repository;

    @PostConstruct
    protected void postConstruct() {
        workExecutorService = Executors.newFixedThreadPool(workerThreadCount);

        scheduledCheckWorkExecutorService.scheduleAtFixedRate(new PDSExecutionWatcherRunnable(), 300, 1000, TimeUnit.MILLISECONDS);
    }

    private class PDSExecutionWatcherRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (jobsInQueue) {
                List<UUID> doneList = new ArrayList<>(0);
                Iterator<Entry<UUID, Future<PDSExecutionCallResult>>> it = jobsInQueue.entrySet().iterator();

                while (it.hasNext()) {

                    Entry<UUID, Future<PDSExecutionCallResult>> entry = it.next();
                    Future<PDSExecutionCallResult> future = entry.getValue();
                    if (future.isDone()) {

                        handleWorkDone(entry, future);
                        doneList.add(entry.getKey());
                        return;
                    }
                }

                for (UUID uuid : doneList) {
                    jobsInQueue.remove(uuid);
                }
            }
        }

        private void handleWorkDone(Entry<UUID, Future<PDSExecutionCallResult>> entry, Future<PDSExecutionCallResult> future) {
            Optional<PDSJob> jobOption = repository.findById(entry.getKey());
            if (jobOption.isPresent()) {

                PDSJob job = jobOption.get();
                if (future.isCancelled()) {
                    job.setState(PDSJobStatusState.CANCELED);
                } else {
                    PDSExecutionCallResult callResult;
                    try {
                        callResult = future.get();
                        job.setResult(callResult.result);
                        job.setState(PDSJobStatusState.DONE);
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error("Job does no longer exist with uuid:{}, but result available!", entry.getKey());
                        job.setState(PDSJobStatusState.FAILED);
                    }
                }
                repository.save(job);

            } else {
                LOG.error("Job does no longer exist with uuid:{}, but result available!", entry.getKey());
            }
        }

    }

    public boolean isQueueFull() {
        synchronized (jobsInQueue) {
            return jobsInQueue.size() >= queueMax;
        }
    }

    public void addToQueue(PDSJob pdsJob) {
        UUID jobUUID = pdsJob.getUUID();
        Future<?> former = null;
        synchronized (jobsInQueue) {
            LOG.debug("add job to execution queue:{}", jobUUID);

            Future<PDSExecutionCallResult> future = workExecutorService.submit(new PDSExecutionCallable(pdsJob));
            former = jobsInQueue.put(jobUUID, future);
        }
        handleFormerJob(jobUUID, former);
    }

    private void handleFormerJob(UUID jobUUID, Future<?> former) {
        if (former == null) {
            return;
        }
        LOG.error("Did found former job in queue with same job UUID:{}", jobUUID);
        former.cancel(true);
        LOG.info("Canceled former job {}", jobUUID);

    }

    public boolean cancel(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        synchronized (jobsInQueue) {

            Iterator<Entry<UUID, Future<PDSExecutionCallResult>>> it = jobsInQueue.entrySet().iterator();
            while (it.hasNext()) {
                Entry<UUID, Future<PDSExecutionCallResult>> entry = it.next();
                if (jobUUID.equals(entry.getKey())) {
                    entry.getValue().cancel(true);
                    return true;
                }
            }
            return false;
        }
    }

}
