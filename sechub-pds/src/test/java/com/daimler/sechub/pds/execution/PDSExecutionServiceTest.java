package com.daimler.sechub.pds.execution;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;
import com.daimler.sechub.pds.job.PDSJobTestHelper;
import com.daimler.sechub.pds.job.PDSUpdateJobTransactionService;
import com.daimler.sechub.pds.job.PDSWorkspaceService;

public class PDSExecutionServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionServiceTest.class);

    private PDSExecutionService serviceToTest;
    private PDSJobRepository repository;
    private PDSExecutionCallableFactory executionCallableFactory;
    private PDSExecutionResult result1;

    @Before
    public void before() throws Exception {
        repository = mock(PDSJobRepository.class);
        executionCallableFactory = mock(PDSExecutionCallableFactory.class);

        result1 = new PDSExecutionResult();

        serviceToTest = new PDSExecutionService();
        serviceToTest.watcherDisabled = true;
        serviceToTest.repository = repository;
        serviceToTest.executionCallableFactory = executionCallableFactory;
    }

    @After
    public void after() {
        /*
         * destroy executor service - to prevent too much memory/thread consumption in
         * tests, not necesary in real world
         */
        serviceToTest.workers.shutdownNow();
        serviceToTest.scheduler.shutdownNow();
    }

    private class TestPDSExecutionCallable extends PDSExecutionCallable {

        private long waitMillis;
        private PDSExecutionResult result;
        private boolean prepareCancelCalled;

        public TestPDSExecutionCallable(long waitMillis, PDSExecutionResult result) {
            super(mock(PDSJob.class), mock(PDSUpdateJobTransactionService.class), mock(PDSWorkspaceService.class));
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
        void prepareForCancel(boolean mayInterruptIfRunning) {
            this.prepareCancelCalled=true;
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
        PDSJob job1 = PDSJobTestHelper.createTestJob(uuid1);
        when(executionCallableFactory.createCallable(job1)).thenReturn(new TestPDSExecutionCallable(100, result1));

        /* execute */
        serviceToTest.addToExecutionQueue(job1);

        assertTrue(serviceToTest.isQueueFull());

        /* test */
        assertQueueNoLongerAndNotTimedOut(10, 300);

    }

    @Test
    public void adding_job_to_queue_sets_status_of_job_to_QUEUED() throws Exception {

        /* prepare */
        serviceToTest.queueMax=5;serviceToTest.postConstruct(); // simulate spring boot container...
        
        UUID uuid1 = UUID.randomUUID();
        PDSJob job1 = PDSJobTestHelper.createTestJob(uuid1);
        assertEquals(PDSJobStatusState.CREATED,job1.getState());
        when(executionCallableFactory.createCallable(job1)).thenReturn(new TestPDSExecutionCallable(500,result1));
        
        /* execute */
        serviceToTest.addToExecutionQueue(job1);
        
        /* test */
        assertEquals(PDSJobStatusState.QUEUED,job1.getState());
        
    }
    
    @Test
    public void adding_jobs_to_queue_status_contains_expected_values() throws Exception {
        /* prepare */
        serviceToTest.queueMax = 5;
        serviceToTest.postConstruct(); // simulate spring boot container...
        UUID uuid1 = UUID.randomUUID();
        PDSJob job1 = PDSJobTestHelper.createTestJob(uuid1);
        when(executionCallableFactory.createCallable(job1)).thenReturn(new TestPDSExecutionCallable(0, result1));

        UUID uuid2 = UUID.randomUUID();
        PDSJob job2 = PDSJobTestHelper.createTestJob(uuid2);
        when(executionCallableFactory.createCallable(job2)).thenReturn(new TestPDSExecutionCallable(500, result1));

        UUID uuid3 = UUID.randomUUID();
        PDSJob job3 = PDSJobTestHelper.createTestJob(uuid3);
        when(executionCallableFactory.createCallable(job3)).thenReturn(new TestPDSExecutionCallable(500, result1));

        when(repository.findById(uuid1)).thenReturn(Optional.of(job1));
        when(repository.findById(uuid2)).thenReturn(Optional.of(job2));
        when(repository.findById(uuid3)).thenReturn(Optional.of(job3));
        
        serviceToTest.addToExecutionQueue(job1);
        serviceToTest.addToExecutionQueue(job2);
        serviceToTest.addToExecutionQueue(job3);
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
        assertEquals(job1,entry1.job);
        assertEquals(uuid1,entry1.jobUUID);
        
        PDSExecutionJobInQueueStatusEntry entry2 = it.next();
        assertTrue(entry2.done);
        assertTrue(entry2.canceled);
        assertEquals(job2,entry2.job);
        assertEquals(uuid2,entry2.jobUUID);
        
        PDSExecutionJobInQueueStatusEntry entry3 = it.next();
        assertFalse(entry3.done);
        assertFalse(entry3.canceled);
        assertEquals(job3,entry3.job);
        assertEquals(uuid3,entry3.jobUUID);
        

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
