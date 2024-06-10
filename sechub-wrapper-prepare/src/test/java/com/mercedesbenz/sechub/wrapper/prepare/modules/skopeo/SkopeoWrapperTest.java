// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.commons.pds.ProcessBuilderFactory;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.test.TestUtil;

class SkopeoWrapperTest {

    private SkopeoWrapper wrapperToTest;
    private PDSProcessAdapterFactory processAdapterFactory;
    private Path workingDirectory;
    private PDSLogSanitizer logSanitizer;
    private ProcessAdapter processAdapter1;
    private ProcessBuilderFactory processBuilderFactory;
    private ProcessBuilder processBuilder1;
    private ProcessBuilder processBuilder2;
    private ProcessAdapter processAdapter2;
    private SkopeoLocationConverter locationConverter;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        wrapperToTest = new SkopeoWrapper();
        workingDirectory = TestUtil.createTempDirectoryInBuildFolder("skopeo-wrapper-test");

        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        logSanitizer = mock(PDSLogSanitizer.class);
        locationConverter = mock(SkopeoLocationConverter.class);
        when(locationConverter.convertLocationForLogin(any())).thenReturn("login-location");
        when(locationConverter.convertLocationForDownload(any())).thenReturn("download-location");

        processAdapter1 = mock(ProcessAdapter.class);
        processAdapter2 = mock(ProcessAdapter.class);

        processBuilderFactory = mock(ProcessBuilderFactory.class);

        processBuilder1 = mock(ProcessBuilder.class);
        processBuilder2 = mock(ProcessBuilder.class);

        when(processAdapterFactory.startProcess(processBuilder1)).thenReturn(processAdapter1);
        when(processAdapterFactory.startProcess(processBuilder2)).thenReturn(processAdapter2);

        when(processAdapter1.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);
        when(processAdapter2.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);
        when(processBuilderFactory.createForCommandList(any())).thenReturn(processBuilder1).thenReturn(processBuilder2);

        wrapperToTest.processAdapterFactory = processAdapterFactory;
        wrapperToTest.processBuilderFactory = processBuilderFactory;
        wrapperToTest.logSanitizer = logSanitizer;
        wrapperToTest.locationConverter = locationConverter;
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
        verify(processAdapterFactory, times(1)).startProcess(processBuilder1);
        verify(processBuilderFactory, times(1)).createForCommandList(any());
        verify(locationConverter).convertLocationForDownload(location);
        assertProcessBuilderFactoryCalledWithParametersForSkopeoDownload(context, "download-location", processBuilderFactory, false);
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

        /* test 1 - login */
        verify(processBuilderFactory, times(2)).createForCommandList(any()); // for login + download

        /* test 2 - login */
        verify(locationConverter).convertLocationForLogin(location);
        assertProcessBuilderFactoryCalledWithParametersForLogin(context, "login-location", username, processBuilderFactory);

        // check user input handling for passwords work as expected
        verify(processBuilder1, never()).inheritIO(); // we do not want this, because enter input would not work on process adapter
        verify(processBuilder1, never()).redirectInput();
        verify(processBuilder1, never()).redirectInput(any(Redirect.class));
        verify(processBuilder1).redirectError(Redirect.INHERIT);
        verify(processBuilder1).redirectOutput(Redirect.INHERIT);

        verify(processAdapter1).enterInput(password.toCharArray()); // process adapter sends the password to the stream

        /* test 3 - download */
        verify(locationConverter).convertLocationForDownload(location);
        assertProcessBuilderFactoryCalledWithParametersForSkopeoDownload(context, "download-location", processBuilderFactory, true);
        verify(processBuilder2).inheritIO(); // all streams shall redirect, here okay

        verify(processAdapter2, never()).enterInput(any());
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
        assertEquals("Error while executing Skopeo download process for: sanitized-location", exception.getMessage());

    }

    private void assertProcessBuilderFactoryCalledWithParametersForLogin(SkopeoContext context, String location, String user,
            ProcessBuilderFactory processBuilderFactory) {

        // skopeo, login, ubuntu:22.04, --username, username1, --password, password1,
        // --authfile, authentication.json]
        List<String> expectedCommands = new ArrayList<>();
        expectedCommands.add("skopeo");
        expectedCommands.add("login");
        expectedCommands.add(location);
        expectedCommands.add("--username");
        expectedCommands.add(user);
        expectedCommands.add("--password-stdin");
        expectedCommands.add("--authfile");
        expectedCommands.add("authentication.json"); // this file will be generated by login mechanism

        verify(processBuilderFactory).createForCommandList(expectedCommands);
    }

    private void assertProcessBuilderFactoryCalledWithParametersForSkopeoDownload(SkopeoContext context, String location,
            ProcessBuilderFactory processBuilderFactory, boolean authorized) {

        List<String> expectedCommands = new ArrayList<>();
        expectedCommands.add("skopeo");
        expectedCommands.add("copy");
        expectedCommands.add(location);
        expectedCommands.add("docker-archive:" + context.getDownloadTarFile().toString());
        if (authorized) {
            expectedCommands.add("--authfile");
            expectedCommands.add("authentication.json");
        }

        verify(processBuilderFactory).createForCommandList(expectedCommands);
    }

}