// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.pds.job.JobConfigurationData;
import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.job.WorkspaceLocationData;

class PDSExecutionCallableTest {

    private PDSJobTransactionService jobTransactionService;
    private PDSWorkspaceService workspaceService;
    private PDSExecutionEnvironmentService environmentService;
    private PDSCheckJobStatusService jobStatusService;
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
    private File metaDataFile;

    @BeforeEach
    void beforeEach() throws IOException {
        jobUUID = UUID.randomUUID();
        workspaceTestDataJob1Folder = new File("./src/test/resources/testdata/workspace1/job1");
        resultFile = new File(workspaceTestDataJob1Folder, "result.txt");
        errorFile = new File(workspaceTestDataJob1Folder, "error.txt");
        outputFile = new File(workspaceTestDataJob1Folder, "output.txt");
        metaDataFile = new File(workspaceTestDataJob1Folder, "metadata.txt");
        messageFolder = new File(workspaceTestDataJob1Folder, "messages");

        assertTrue(messageFolder.exists());
        assertTrue(resultFile.exists());
        assertTrue(outputFile.exists());
        assertTrue(errorFile.exists());

        jobTransactionService = mock(PDSJobTransactionService.class);
        workspaceService = mock(PDSWorkspaceService.class);
        environmentService = mock(PDSExecutionEnvironmentService.class);
        jobStatusService = mock(PDSCheckJobStatusService.class);
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        processAdapter = mock(ProcessAdapter.class);

        data = mock(JobConfigurationData.class);
        locationData = mock(WorkspaceLocationData.class);
        when(locationData.getWorkspaceLocation()).thenReturn(resultFile.getParent());
        when(locationData.getResultFileLocation()).thenReturn(resultFile.getAbsolutePath());
        when(locationData.getUserMessagesLocation()).thenReturn(messageFolder.getAbsolutePath());
        when(locationData.getMetaDataFileLocation()).thenReturn(metaDataFile.getAbsolutePath());
        when(locationData.getSourceCodeZipFileLocation()).thenReturn("not-defined-src-zip");
        when(locationData.getBinariesTarFileLocation()).thenReturn("not-defined-bin-tar");
        when(locationData.getExtractedBinariesLocation()).thenReturn("not-defined-bin-extraction");
        when(locationData.getExtractedSourcesLocation()).thenReturn("not-defined-src-extraction");

        when(workspaceService.getSystemErrorFile(jobUUID)).thenReturn(errorFile);
        when(workspaceService.getSystemOutFile(jobUUID)).thenReturn(outputFile);
        when(workspaceService.getMetaDataFile(jobUUID)).thenReturn(metaDataFile);
        when(workspaceService.getResultFile(jobUUID)).thenReturn(resultFile);

        when(data.getJobConfigurationJson()).thenReturn("{}");

        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(jobTransactionService.getJobConfigurationData(jobUUID)).thenReturn(data);
        when(workspaceService.getProductPathFor(any())).thenReturn("the/path/to/product.sh");
        when(workspaceService.createLocationData(jobUUID)).thenReturn(locationData);
        when(workspaceService.getMessagesFolder(jobUUID)).thenReturn(messageFolder);

        callableToTest = new PDSExecutionCallable(jobUUID, jobTransactionService, workspaceService, environmentService, jobStatusService,
                processAdapterFactory);

        when(processAdapter.isAlive()).thenReturn(true);

    }

    @Test
    void minutes_0_from_workspace_fails_without_job_transaction_write() throws Exception {
        /* prepare */
        simulateProcessTimeOut(0L);

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertTrue(result.failed);
        // internally an illegal state is thrown before any execution,
        verify(processAdapterFactory, never()).startProcess(any());
        verify(jobTransactionService, never()).updateJobExecutionDataInOwnTransaction(any(), any());
    }

    @Test
    void a_timeout_does_write_execution_result_data() throws Exception {
        /* prepare */
        simulateProcessTimeOut();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertTrue(result.failed);
        assertEquals("Product time out.", result.result);

        ArgumentCaptor<PDSExecutionData> executionDataCaptor = ArgumentCaptor.forClass(PDSExecutionData.class);
        verify(jobTransactionService).updateJobExecutionDataInOwnTransaction(eq(jobUUID), executionDataCaptor.capture());

        PDSExecutionData executionData = executionDataCaptor.getValue();
        assertEquals("the output", executionData.getOutputStreamData());
        assertEquals("an error", executionData.getErrorStreamData());
        assertEquals("meta data", executionData.getMetaData());

    }

    @Test
    void no_timeout_does_not_fail_and_writes_execution_result_data() throws Exception {
        /* prepare */
        simulateProcessDone();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertFalse(result.failed);
        assertEquals("the result", result.result);

        ArgumentCaptor<PDSExecutionData> executionDataCaptor = ArgumentCaptor.forClass(PDSExecutionData.class);
        verify(jobTransactionService).updateJobExecutionDataInOwnTransaction(eq(jobUUID), executionDataCaptor.capture());

        PDSExecutionData executionData = executionDataCaptor.getValue();
        assertEquals("the output", executionData.getOutputStreamData());
        assertEquals("an error", executionData.getErrorStreamData());
        assertEquals("meta data", executionData.getMetaData());
    }

    @Test
    void no_timeout_does_not_fail_and_writes_messages() throws Exception {
        /* prepare */
        simulateProcessDone();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertFalse(result.failed);
        assertJob1MessageHasBeenPersistedToDB();

    }

    @Test
    void a_timeout_does_fail_and_writes_messages() throws Exception {
        /* prepare */
        simulateProcessTimeOut();

        /* execute */
        PDSExecutionResult result = callableToTest.call();

        /* test */
        assertTrue(result.failed);
        assertJob1MessageHasBeenPersistedToDB();

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
        when(workspaceService.getMinutesToWaitForResult(any())).thenReturn(timeoutInMinutes);
        when(processAdapter.waitFor(timeoutInMinutes, TimeUnit.MINUTES)).thenReturn(true);
    }

    private void simulateProcessTimeOut() throws InterruptedException {
        simulateProcessTimeOut(1L);
    }

    private void simulateProcessTimeOut(long timeoutInMinutes) throws InterruptedException {
        when(workspaceService.getMinutesToWaitForResult(any())).thenReturn(timeoutInMinutes);
        when(processAdapter.waitFor(timeoutInMinutes, TimeUnit.MINUTES)).thenReturn(false);
    }
}
