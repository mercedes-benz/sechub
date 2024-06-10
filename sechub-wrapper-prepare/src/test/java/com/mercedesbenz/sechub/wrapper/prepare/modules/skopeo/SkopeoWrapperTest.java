// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.test.TestUtil;

class SkopeoWrapperTest {

    private SkopeoWrapper wrapperToTest;
    private PDSProcessAdapterFactory processAdapterFactory;
    private Path workingDirectory;
    private PDSLogSanitizer logSanitizer;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        wrapperToTest = new SkopeoWrapper();
        workingDirectory = TestUtil.createTempDirectoryInBuildFolder("skopeo-wrapper-test");

        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        logSanitizer = mock(PDSLogSanitizer.class);

        ProcessAdapter processAdapter = mock(ProcessAdapter.class);
        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(processAdapter.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);

        wrapperToTest.processAdapterFactory = processAdapterFactory;
        wrapperToTest.logSanitizer = logSanitizer;
    }

    @Test
    void when_download_is_executed_download_process_is_executed() throws IOException {
        /* prepare */
        String location = "docker://ubuntu:22.04";

        SkopeoContext context = new SkopeoContext();
        context.setLocation(location);
        context.init(workingDirectory);

        /* execute */
        wrapperToTest.download(context);

        /* test */
        ArgumentCaptor<ProcessBuilder> pbCaptor = ArgumentCaptor.forClass(ProcessBuilder.class);
        verify(processAdapterFactory, times(1)).startProcess(pbCaptor.capture());
        assertProcessBuilderCalledWithParametersForSkopeoDownload(context, location, pbCaptor.getValue(), false);
    }

    @Test
    void when_download_is_executed_with_credentials_download_and_login_process_are_executed() throws IOException {
        /* prepare */
        String location = "docker://ubuntu:22.04";
        String username = "username1";
        String password = "password1";

        SkopeoContext context = new SkopeoContext();
        context.setLocation(location);
        context.init(workingDirectory);
        context.setSealedCredentials(username, password);

        /* execute */
        wrapperToTest.download(context);

        /* test */
        ArgumentCaptor<ProcessBuilder> pbCaptor = ArgumentCaptor.forClass(ProcessBuilder.class);
        verify(processAdapterFactory, times(3)).startProcess(pbCaptor.capture());
        List<ProcessBuilder> processBuilders = pbCaptor.getAllValues();
        Iterator<ProcessBuilder> pbIt = processBuilders.iterator();

        assertProcessBuilderCalledWithParametersForLogin(context, "ubuntu:22.04", username, password, pbIt.next());
        assertProcessBuilderCalledWithParametersForSkopeoDownload(context, location, pbIt.next(), true);
    }

    @Test
    void when_process_throws_exception_then_download_throws_exception() throws IOException {
        /* prepare */
        String location = "docker://ubuntu:22.04";
        SkopeoContext context = new SkopeoContext();
        context.setLocation(location);
        context.init(workingDirectory);
        when(processAdapterFactory.startProcess(any())).thenThrow(new IOException());

        when(logSanitizer.sanitize(eq(location), any(Integer.class))).thenReturn("sanitized-location");

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> wrapperToTest.download(context));

        /* test */
        assertEquals("Error while starting Skopeo download process for: sanitized-location", exception.getMessage());

    }

    private void assertProcessBuilderCalledWithParametersForLogin(SkopeoContext context, String location, String user, String password,
            ProcessBuilder calledProcessBuilder) {

        List<String> commandList = calledProcessBuilder.command();

        // skopeo, login, ubuntu:22.04, --username, username1, --password, password1,
        // --authfile, authentication.json]
        List<String> expectedCommands = new ArrayList<>();
        expectedCommands.add("skopeo");
        expectedCommands.add("login");
        expectedCommands.add(location);
        expectedCommands.add("--username");
        expectedCommands.add(user);
        expectedCommands.add("--password"); // idea: input stream?
        expectedCommands.add(password); // "$ENV_MY_PASSWORD"
        expectedCommands.add("--authfile");
        expectedCommands.add("authentication.json"); // json is from login output file...

        assertCommandListAsExpected(commandList, expectedCommands);
    }

    private void assertProcessBuilderCalledWithParametersForSkopeoDownload(SkopeoContext context, String location, ProcessBuilder calledProcessBuilder,
            boolean authorized) {

        List<String> commandList = calledProcessBuilder.command();

        List<String> expectedCommands = new ArrayList<>();
        expectedCommands.add("skopeo");
        expectedCommands.add("copy");
        expectedCommands.add(location);
        expectedCommands.add("docker-archive:" + context.getDownloadTarFile().toString());
        if (authorized) {
            expectedCommands.add("--authfile");
            expectedCommands.add("authentication.json");
        }

        assertCommandListAsExpected(commandList, expectedCommands);
    }

    private void assertCommandListAsExpected(List<String> commandList, List<String> expectedCommands) {
        if (expectedCommands.size() != commandList.size()) {
            assertEquals(expectedCommands.toString(), commandList.toString());
        }
        int pos = 0;
        for (String expectedCommand : expectedCommands) {
            String command = commandList.get(pos++);
            if (!command.equals(expectedCommand)) {
                assertEquals(expectedCommand, command, "Command differs!");
            }

        }
    }

}