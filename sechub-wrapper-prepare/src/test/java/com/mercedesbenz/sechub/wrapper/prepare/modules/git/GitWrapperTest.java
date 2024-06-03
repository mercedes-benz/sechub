// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

class GitWrapperTest {

    private GitWrapper wrapperToTest;
    private PDSProcessAdapterFactory processAdapterFactory;
    private JGitAdapter jGitAdapter;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        wrapperToTest = new GitWrapper();
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        ProcessAdapter processAdapter = mock(ProcessAdapter.class);
        jGitAdapter = mock(JGitAdapter.class);
        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(processAdapter.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);
        doNothing().when(jGitAdapter).clone(any());

        wrapperToTest.processAdapterFactory = processAdapterFactory;
        wrapperToTest.jGitAdapter = jGitAdapter;
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_cloneRepository_is_executed_the_processAdapterFactory_starts_JGit_clone(String location) throws IOException {
        /* prepare */
        Path tempDir = Files.createTempDirectory("test");
        GitContext gitContext = new GitContext();
        gitContext.setCloneWithoutHistory(true);
        gitContext.setLocation(location);
        gitContext.setSealedCredentials("user", "password");
        gitContext.init(tempDir);

        /* execute */
        wrapperToTest.downloadRemoteData(gitContext);

        /* test */
        verify(jGitAdapter, times(1)).clone(gitContext);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_JGit_clone_throws_exception_RuntimeException_is_thrown(String location) {
        /* prepare */
        RuntimeException runtimeException = new RuntimeException("Error while cloning from repository: " + location);
        doThrow(runtimeException).when(jGitAdapter).clone(any());

        /* execute */
        RuntimeException exception = assertThrows(RuntimeException.class, () -> wrapperToTest.downloadRemoteData(any()));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cloning from repository: " + location));
    }

    @Test
    void when_cleanGitDirectory_is_executed_the_processAdapterFactory_starts_one_process() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";

        /* execute */
        wrapperToTest.cleanUploadDirectory(Path.of(directory));

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());

    }

    @Test
    void when_cleanGitDirectory_throws_exception_IOException_is_thrown() throws IOException {
        /* prepare */
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);
        String directory = "test-upload-folder";

        /* execute */
        Exception exception = assertThrows(Exception.class, () -> wrapperToTest.cleanUploadDirectory(Path.of(directory)));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cleaning git directory: test-upload-folder"));
    }

}