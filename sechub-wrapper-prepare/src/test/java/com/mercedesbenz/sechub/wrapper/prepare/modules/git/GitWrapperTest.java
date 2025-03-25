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

import com.mercedesbenz.sechub.commons.archive.DirectoryAndFileSupport;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

class GitWrapperTest {

    private GitWrapper wrapperToTest;
    private PDSProcessAdapterFactory processAdapterFactory;
    private JGitAdapter jGitAdapter;
    private DirectoryAndFileSupport directoryAndFileSupport;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        ProcessAdapter processAdapter = mock(ProcessAdapter.class);
        jGitAdapter = mock(JGitAdapter.class);
        directoryAndFileSupport = mock(DirectoryAndFileSupport.class);

        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(processAdapter.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);
        doNothing().when(jGitAdapter).clone(any());

        wrapperToTest = new GitWrapper(jGitAdapter, processAdapterFactory, directoryAndFileSupport);
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
    void when_removeGitFiles_is_executed_the_clean_directories_is_called() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";

        /* execute */
        Path path = Path.of(directory);
        wrapperToTest.removeAdditionalGitFiles(path);

        /* test */
        verify(directoryAndFileSupport, times(1)).cleanDirectories(path.toFile(), AutoCleanupAdditionalGitFilesFilter.INSTANCE);

    }

    @Test
    void when_removeGitFiles_is_executed_and_clean_directories_throws_exception_it_is_rethrown() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";
        Path pathOfDir = Path.of(directory);
        doThrow(new IOException("test exception")).when(directoryAndFileSupport).cleanDirectories(eq(pathOfDir.toFile()), any());

        /* execute */
        Exception exception = assertThrows(Exception.class, () -> wrapperToTest.removeAdditionalGitFiles(pathOfDir));

        /* test */
        String message = exception.getMessage();
        if (!message.contains("test exception")) {
            fail("Wrong message from exception: " + message);
        }
    }

    @Test
    void when_removeGitFolder_is_executed_the_clean_directories_is_called() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";

        /* execute */
        Path path = Path.of(directory);
        wrapperToTest.removeGitFolders(path);

        /* test */
        verify(directoryAndFileSupport, times(1)).cleanDirectories(path.toFile(), AutoCleanupGitFoldersFilter.INSTANCE);

    }

    @Test
    void when_removeGitFolder_is_executed_and_clean_directories_throws_exception_it_is_rethrown() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";
        Path pathOfDir = Path.of(directory);
        doThrow(new IOException("test exception")).when(directoryAndFileSupport).cleanDirectories(eq(pathOfDir.toFile()), any());

        /* execute */
        Exception exception = assertThrows(Exception.class, () -> wrapperToTest.removeGitFolders(pathOfDir));

        /* test */
        String message = exception.getMessage();
        if (!message.contains("test exception")) {
            fail("Wrong message from exception: " + message);
        }
    }

}