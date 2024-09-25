// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionException;
import com.mercedesbenz.sechub.pds.job.JobConfigurationData;
import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSGetJobStreamService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspacePreparationResult;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.job.WorkspaceLocationData;

class PDSExecutionCallableTest {

    private PDSJobTransactionService jobTransactionService;
    private PDSWorkspaceService workspaceService;
    private PDSExecutionEnvironmentService environmentService;
    private PDSCheckJobStatusService jobStatusService;
    private PDSGetJobStreamService pdsGetJobStreamService;
    private UUID jobUUID;
    private PDSExecutionCallable callableToTest;
    private PDSProcessAdapterFactory processAdapterFactory;
    private ProcessAdapter processAdapter;
    private JobConfigurationData data;
    private WorkspaceLocationData locationData;
    private File workspaceTestDataJob1Folder;
    private File resultFile;
    private File errorFile;
    private File outputFile;
    private File messageFolder;
    private File eventsFolder;
    private File metaDataFile;
    private ProcessHandlingDataFactory processHandlingFactory;
    private ProductLaunchProcessHandlingData launchProcessHandlingData;

    @BeforeEach
    void beforeEach() throws Exception {
        jobUUID = UUID.randomUUID();
        workspaceTestDataJob1Folder = new File("./src/test/resources/testdata/workspace1/job1");
        resultFile = new File(workspaceTestDataJob1Folder, "result.txt");
        errorFile = new File(workspaceTestDataJob1Folder, "error.txt");
        outputFile = new File(workspaceTestDataJob1Folder, "output.txt");
        metaDataFile = new File(workspaceTestDataJob1Folder, "metadata.txt");
        messageFolder = new File(workspaceTestDataJob1Folder, "messages");
        eventsFolder = new File(workspaceTestDataJob1Folder, "events");

        assertTrue(messageFolder.exists());
        assertTrue(resultFile.exists());
        assertTrue(outputFile.exists());
        assertTrue(errorFile.exists());

        jobTransactionService = mock(PDSJobTransactionService.class);
        workspaceService = mock(PDSWorkspaceService.class);
        environmentService = mock(PDSExecutionEnvironmentService.class);
        jobStatusService = mock(PDSCheckJobStatusService.class);
        pdsGetJobStreamService = mock(PDSGetJobStreamService.class);
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        processAdapter = mock(ProcessAdapter.class);
        processHandlingFactory = mock(ProcessHandlingDataFactory.class);

        data = mock(JobConfigurationData.class);
        locationData = mock(WorkspaceLocationData.class);
        when(locationData.getWorkspaceLocation()).thenReturn(resultFile.getParent());
        when(locationData.getResultFileLocation()).thenReturn(resultFile.getAbsolutePath());
        when(locationData.getUserMessagesLocation()).thenReturn(messageFolder.getAbsolutePath());
        when(locationData.getMetaDataFileLocation()).thenReturn(metaDataFile.getAbsolutePath());
        when(locationData.getEventsLocation()).thenReturn(eventsFolder.getAbsolutePath());

        when(locationData.getSourceCodeZipFileLocation()).thenReturn("not-defined-src-zip");
        when(locationData.getBinariesTarFileLocation()).thenReturn("not-defined-bin-tar");
        when(locationData.getExtractedBinariesLocation()).thenReturn("not-defined-bin-extraction");
        when(locationData.getExtractedSourcesLocation()).thenReturn("not-defined-src-extraction");

        when(workspaceService.getSystemErrorFile(jobUUID)).thenReturn(errorFile);
        when(workspaceService.getSystemOutFile(jobUUID)).thenReturn(outputFile);
        when(workspaceService.getMetaDataFile(jobUUID)).thenReturn(metaDataFile);
        when(workspaceService.getResultFile(jobUUID)).thenReturn(resultFile);

        PDSWorkspacePreparationResult launchScriptExecutableResult = new PDSWorkspacePreparationResult(true);
        when(workspaceService.prepare(eq(jobUUID), any(), any())).thenReturn(launchScriptExecutableResult);

        PDSJobConfiguration configuration = new PDSJobConfiguration();
        when(data.getJobConfiguration()).thenReturn(configuration);

        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(jobTransactionService.getJobConfigurationDataOrFail(jobUUID)).thenReturn(data);
        when(workspaceService.getProductPathFor(any())).thenReturn("the/path/to/product.sh");
        when(workspaceService.createLocationData(jobUUID)).thenReturn(locationData);
        when(workspaceService.getMessagesFolder(jobUUID)).thenReturn(messageFolder);

        launchProcessHandlingData = mock(ProductLaunchProcessHandlingData.class);
        when(processHandlingFactory.createForLaunchOperation(any())).thenReturn(launchProcessHandlingData);

        PDSExecutionCallableServiceCollection serviceCollection = mock(PDSExecutionCallableServiceCollection.class);
        when(serviceCollection.getEnvironmentService()).thenReturn(environmentService);
        when(serviceCollection.getJobStatusService()).thenReturn(jobStatusService);
        when(serviceCollection.getPdsGetJobStreamService()).thenReturn(pdsGetJobStreamService);
        when(serviceCollection.getJobTransactionService()).thenReturn(jobTransactionService);
        when(serviceCollection.getProcessAdapterFactory()).thenReturn(processAdapterFactory);
        when(serviceCollection.getWorkspaceService()).thenReturn(workspaceService);
        when(serviceCollection.getProcessHandlingDataFactory()).thenReturn(processHandlingFactory);

        callableToTest = new PDSExecutionCallable(jobUUID, serviceCollection);

        when(processAdapter.isAlive()).thenReturn(true);

    }

    @Test
    void timeout_with_0_fails_without_job_transaction_write() throws Exception {
        /* prepare */
        simulateProcessTimeOut(0L);

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertJobHasMarkedAsRunningInOwnTransaction();
        assertTrue(result.isFailed());
        // internally an illegal state is thrown before any execution,
        verify(processAdapterFactory, never()).startProcess(any());
        verify(jobTransactionService, never()).updateJobExecutionDataInOwnTransaction(any(), any());
        // check there is no wait for a process at all
        verify(processAdapter, never()).waitFor(anyLong(), any());
        assertFalse(result.isEncryptionFailure());
    }

    @Test
    void a_timeout_does_write_execution_result_data() throws Exception {
        /* prepare */
        simulateProcessTimeOut();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertJobHasMarkedAsRunningInOwnTransaction();
        assertTrue(result.isFailed());
        assertEquals("Product time out.", result.getResult());

        ArgumentCaptor<PDSExecutionData> executionDataCaptor = ArgumentCaptor.forClass(PDSExecutionData.class);
        verify(jobTransactionService).updateJobExecutionDataInOwnTransaction(eq(jobUUID), executionDataCaptor.capture());

        PDSExecutionData executionData = executionDataCaptor.getValue();
        assertEquals("the output", executionData.getOutputStreamData());
        assertEquals("an error", executionData.getErrorStreamData());
        assertEquals("meta data", executionData.getMetaData());
        assertFalse(result.isEncryptionFailure());

    }

    @Test
    void no_timeout_does_not_fail_and_writes_execution_result_data() throws Exception {
        /* prepare */
        simulateProcessDone();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertJobHasMarkedAsRunningInOwnTransaction();
        assertFalse(result.isFailed());
        assertEquals("the result", result.getResult());

        ArgumentCaptor<PDSExecutionData> executionDataCaptor = ArgumentCaptor.forClass(PDSExecutionData.class);
        verify(jobTransactionService).updateJobExecutionDataInOwnTransaction(eq(jobUUID), executionDataCaptor.capture());

        PDSExecutionData executionData = executionDataCaptor.getValue();
        assertEquals("the output", executionData.getOutputStreamData());
        assertEquals("an error", executionData.getErrorStreamData());
        assertEquals("meta data", executionData.getMetaData());
        assertFalse(result.isEncryptionFailure());
    }

    @Test
    void no_timeout_does_not_fail_and_writes_messages() throws Exception {
        /* prepare */
        simulateProcessDone();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertJobHasMarkedAsRunningInOwnTransaction();
        assertFalse(result.isFailed());
        assertJob1MessageHasBeenPersistedToDB();
        assertFalse(result.isEncryptionFailure());
    }

    @Test
    void a_timeout_does_fail_and_writes_messages() throws Exception {
        /* prepare */
        simulateProcessTimeOut();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertJobHasMarkedAsRunningInOwnTransaction();
        assertTrue(result.isFailed());
        assertJob1MessageHasBeenPersistedToDB();
        assertFalse(result.isEncryptionFailure());

    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 4711 })
    void process_waiting_is_done_with_value_from_handling_data_factory_and_time_unit_minutes(int value) throws Exception {
        /* prepare */
        when(launchProcessHandlingData.getMinutesToWaitBeforeProductTimeout()).thenReturn(value);

        /* execute */
        callableToTest.call();

        /* test */
        verify(processAdapter).waitFor(value, TimeUnit.MINUTES);
    }

    @Test
    void when_getJobConfigurationDataOrFail_fails_the_exception_leads_to_a_job_marked_as_encryption_out_of_synch() throws Exception {
        /* prepare */
        when(jobTransactionService.getJobConfigurationDataOrFail(any())).thenThrow(new PDSEncryptionException("some text"));
        simulateProcessDone();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertTrue(result.isEncryptionFailure());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private void assertJobHasMarkedAsRunningInOwnTransaction() {
        verify(jobTransactionService).markJobAsRunningInOwnTransaction(jobUUID);
    }

    private void assertJob1MessageHasBeenPersistedToDB() {
        SecHubMessagesList list = new SecHubMessagesList();
        list.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.INFO, "message1"));

        verify(jobTransactionService).updateJobMessagesInOwnTransaction(eq(jobUUID), eq(list));
    }

    private void simulateProcessDone() throws InterruptedException {
        simulateProcessDone(1);
    }

    private void simulateProcessDone(long timeoutInMinutes) throws InterruptedException {

        when(launchProcessHandlingData.getMinutesToWaitBeforeProductTimeout()).thenReturn((int) timeoutInMinutes);
        when(processAdapter.waitFor(timeoutInMinutes, TimeUnit.MINUTES)).thenReturn(true);
    }

    private void simulateProcessTimeOut() throws InterruptedException {
        simulateProcessTimeOut(1L);
    }

    private void simulateProcessTimeOut(long timeoutInMinutes) throws InterruptedException {
        when(launchProcessHandlingData.getMinutesToWaitBeforeProductTimeout()).thenReturn((int) timeoutInMinutes);
        when(processAdapter.waitFor(timeoutInMinutes, TimeUnit.MINUTES)).thenReturn(false);
    }
}
