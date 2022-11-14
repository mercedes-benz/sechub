// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventData;
import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventType;
import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSJob;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.job.PDSJobTestHelper;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;

public class PDSExecutionServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionServiceTest.class);

    private PDSExecutionService serviceToTest;
    private PDSJobRepository repository;
    private PDSExecutionCallableFactory executionCallableFactory;
    private PDSExecutionResult result1;

    private PDSJobTransactionService updateService;

    private PDSWorkspaceService workspaceService;

    @Before
    public void before() throws Exception {
        repository = mock(PDSJobRepository.class);
        executionCallableFactory = mock(PDSExecutionCallableFactory.class);
        updateService = mock(PDSJobTransactionService.class);
        workspaceService = mock(PDSWorkspaceService.class);

        result1 = new PDSExecutionResult();

        serviceToTest = new PDSExecutionService();
        serviceToTest.watcherDisabled = true;
        serviceToTest.repository = repository;
        serviceToTest.executionCallableFactory = executionCallableFactory;
        serviceToTest.updateService = updateService;
        serviceToTest.workspaceService = workspaceService;
    }

    @After
    public void after() {
        /*
         * destroy executor service - to prevent too much memory/thread consumption in
         * tests, not necessary in real world
         */
        serviceToTest.workers.shutdownNow();
        serviceToTest.scheduler.shutdownNow();
    }

    private class TestPDSExecutionCallable extends PDSExecutionCallable {

        private long waitMillis;
        private PDSExecutionResult result;

        public TestPDSExecutionCallable(UUID jobUUID, long waitMillis, PDSExecutionResult result) {
            super(jobUUID, mock(PDSJobTransactionService.class), mock(PDSWorkspaceService.class), mock(PDSExecutionEnvironmentService.class),
                    mock(PDSCheckJobStatusService.class), mock(PDSProcessAdapterFactory.class));
            this.waitMillis = waitMillis;
            this.result = result;
        }

        @Override
        public PDSExecutionResult call() throws Exception {
            long millis = waitMillis;
            LOG.info("waiting {} ms-START", millis);
            Thread.sleep(millis);
            LOG.info("waiting {} ms-DONE", millis);
            return result;
        }

        @Override
        boolean prepareForCancel(boolean mayInterruptIfRunning) {
            return true;
        }

    }

    @Test
    public void when_service_queuemax_is_zero_queue_is_always_full() {
        /* prepare */
        serviceToTest.queueMax = 0;
        serviceToTest.postConstruct(); // simulate spring boot container...

        /* execute + test */
        assertTrue(serviceToTest.isQueueFull());
    }

    @Test
    public void when_service_queuemax_is_1_queue_having_no_entries_is_not_full() {
        /* prepare */
        serviceToTest.queueMax = 1;
        serviceToTest.postConstruct(); // simulate spring boot container...

        /* execute + test */
        assertFalse(serviceToTest.isQueueFull());
    }

    @Test
    public void when_service_queuemax_is_1_queue_having_one_entry_is_full_after_work_is_done_queue_no_longer_full() throws Exception {
        /* prepare */
        serviceToTest.queueMax = 1;
        serviceToTest.watcherDisabled = false; // enable watcher
        serviceToTest.postConstruct(); // simulate spring boot container...
        UUID uuid1 = UUID.randomUUID();
        when(executionCallableFactory.createCallable(uuid1)).thenReturn(new TestPDSExecutionCallable(uuid1, 100, result1));

        /* execute */
        serviceToTest.addToExecutionQueueAsynchron(uuid1);

        assertTrue(serviceToTest.isQueueFull());

        /* test */
        assertQueueNoLongerAndNotTimedOut(10, 300);

    }

    @Test
    public void adding_jobs_to_queue_status_contains_expected_values() throws Exception {
        /* prepare */
        serviceToTest.queueMax = 5;
        serviceToTest.postConstruct(); // simulate spring boot container...
        UUID uuid1 = UUID.randomUUID();
        PDSJob job1 = PDSJobTestHelper.createTestJobStartedNowCreated3SecondsBefore(uuid1);
        when(executionCallableFactory.createCallable(uuid1)).thenReturn(new TestPDSExecutionCallable(uuid1, 0, result1));

        UUID uuid2 = UUID.randomUUID();
        PDSJob job2 = PDSJobTestHelper.createTestJobStartedNowCreated3SecondsBefore(uuid2);
        when(executionCallableFactory.createCallable(uuid2)).thenReturn(new TestPDSExecutionCallable(uuid2, 500, result1));

        UUID uuid3 = UUID.randomUUID();
        PDSJob job3 = PDSJobTestHelper.createTestJobStartedNowCreated3SecondsBefore(uuid3);
        when(executionCallableFactory.createCallable(uuid3)).thenReturn(new TestPDSExecutionCallable(uuid3, 500, result1));

        when(repository.findById(uuid1)).thenReturn(Optional.of(job1));
        when(repository.findById(uuid2)).thenReturn(Optional.of(job2));
        when(repository.findById(uuid3)).thenReturn(Optional.of(job3));

        serviceToTest.addToExecutionQueueAsynchron(uuid1);
        serviceToTest.addToExecutionQueueAsynchron(uuid2);
        serviceToTest.addToExecutionQueueAsynchron(uuid3);
        serviceToTest.cancel(job2.getUUID());

        Thread.sleep(100); // avoid race codition with job1 - so its always done

        /* execute */
        PDSExecutionStatus status = serviceToTest.getExecutionStatus();

        /* test */
        assertEquals(5, status.queueMax);
        assertEquals(3, status.jobsInQueue);
        assertEquals(3, status.entries.size());
        Iterator<PDSExecutionJobInQueueStatusEntry> it = status.entries.iterator();

        PDSExecutionJobInQueueStatusEntry entry1 = it.next();
        assertTrue(entry1.done);
        assertFalse(entry1.canceled);
        assertEquals(job1.getState(), entry1.state);
        assertEquals(job1.getCreated(), entry1.created);
        assertEquals(job1.getStarted(), entry1.started);
        assertEquals(uuid1, entry1.jobUUID);

        // no cancel event sent
        verify(workspaceService, never()).sendEvent(eq(job1.getUUID()), any());

        PDSExecutionJobInQueueStatusEntry entry2 = it.next();
        assertTrue(entry2.done);
        assertTrue(entry2.canceled);
        assertEquals(job2.getUUID(), entry2.jobUUID);
        assertEquals(job2.getState(), entry2.state);
        assertEquals(job2.getCreated(), entry2.created);
        assertEquals(job2.getStarted(), entry2.started);
        assertEquals(uuid2, entry2.jobUUID);

        // check cancel request execution event is sent
        ArgumentCaptor<ExecutionEventData> eventDataCaptor = ArgumentCaptor.forClass(ExecutionEventData.class);
        verify(workspaceService).sendEvent(eq(job2.getUUID()), eq(ExecutionEventType.CANCEL_REQUESTED), eventDataCaptor.capture());

        ExecutionEventData eventData = eventDataCaptor.getValue();
        assertNotNull(eventData.getCreationTimeStamp());

        PDSExecutionJobInQueueStatusEntry entry3 = it.next();
        assertFalse(entry3.done);
        assertFalse(entry3.canceled);
        assertEquals(job3.getState(), entry3.state);
        assertEquals(job3.getCreated(), entry3.created);
        assertEquals(job3.getStarted(), entry3.started);
        assertEquals(uuid3, entry3.jobUUID);

        // no cancel event sent
        verify(workspaceService, never()).sendEvent(eq(job3.getUUID()), any());
    }

    private void assertQueueNoLongerAndNotTimedOut(int maxLoops, long timeToWaitInMillisPerLoop) throws InterruptedException {
        int count = 0;

        while (serviceToTest.isQueueFull()) {
            count++;
            if (count > maxLoops) {
                fail("Waited " + timeToWaitInMillisPerLoop * count + " so timed out - queue is not handled!");
            }
            Thread.sleep(timeToWaitInMillisPerLoop);
        }
    }

}
