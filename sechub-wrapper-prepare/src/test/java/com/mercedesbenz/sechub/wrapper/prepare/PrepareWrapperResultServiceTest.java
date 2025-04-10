// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

class PrepareWrapperResultServiceTest {
    private PrepareWrapperResultService serviceToTest;
    private PrepareWrapperEnvironment environment;
    private TextFileWriter writer;
    private File resultFile;
    private File messagesFolder;
    private PDSUserMessageSupport messageSupport;

    @BeforeEach
    void beforeEach() throws Exception {
        environment = mock(PrepareWrapperEnvironment.class);
        writer = mock(TextFileWriter.class);
        messageSupport = mock(PDSUserMessageSupport.class);

        serviceToTest = new PrepareWrapperResultService(environment, writer, messageSupport);

        // prepare output directories
        File directory = TestUtil.createTempDirectoryInBuildFolder("wrapper-storage-service").toFile();
        resultFile = new File(directory, "resultfile.txt");
        messagesFolder = new File(directory, "messages");
    }

    @Test
    void service_stores_product_result_as_file_at_defined_location_by_writer() throws Exception {
        /* prepare */
        AdapterExecutionResult result = new AdapterExecutionResult("content");
        when(environment.getPdsResultFile()).thenReturn(resultFile.getAbsolutePath());
        when(environment.getPdsUserMessagesFolder()).thenReturn(messagesFolder.getAbsolutePath());

        /* execute */
        serviceToTest.store(result);

        /* test */
        verify(writer).writeTextToFile(resultFile, "content", true);
    }

    @Test
    void service_stores_product_messages_and_result_and_writes_product_messages() throws Exception {
        /* prepare */
        SecHubMessage message1 = new SecHubMessage(SecHubMessageType.INFO, "message1");
        SecHubMessage message2 = new SecHubMessage(SecHubMessageType.WARNING, "message2");

        AdapterExecutionResult result = new AdapterExecutionResult("content1", Arrays.asList(message1, message2));
        when(environment.getPdsResultFile()).thenReturn(resultFile.getAbsolutePath());
        when(environment.getPdsUserMessagesFolder()).thenReturn(messagesFolder.getAbsolutePath());

        /* execute */
        serviceToTest.store(result);

        /* test */
        verify(writer).writeTextToFile(resultFile, "content1", true);
        verify(messageSupport).writeMessages(eq(Arrays.asList(message1, message2)));
    }
}