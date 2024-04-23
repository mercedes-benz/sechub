package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

class PrepareWrapperGITTest {

    PrepareWrapperGIT gitToTest;

    PDSProcessAdapterFactory processAdapterFactory;

    ProcessAdapter processAdapter;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        gitToTest = new PrepareWrapperGIT();
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        processAdapter = mock(ProcessAdapter.class);
        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(processAdapter.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);

        gitToTest.processAdapterFactory = processAdapterFactory;
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void getRepositoryURL_returns_repositoryURL_with_username_and_password(String location) {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");

        /* execute */
        String repositoryUrl = gitToTest.getRepositoryURL(location);

        /* test */
        assertTrue(repositoryUrl.contains("https://user:password@"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void getRepositoryURL_returns_repositoryURL_when_username_and_password_not_set(String location) {
        /* execute */
        String repositoryUrl = gitToTest.getRepositoryURL(location);

        /* test */
        assertEquals(location, repositoryUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void cloneRepository_returns_mocked_process(String location) throws IOException {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");

        /* execute */
        gitToTest.cloneRepository(location);

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_startProcess_throws_exception_IOexception_is_thrown(String location) throws IOException {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> gitToTest.cloneRepository(location));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cloning repository: "));
    }

    @Test
    void cleanGitDirectory_returns_mocked_process() throws IOException {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");
        ReflectionTestUtils.setField(gitToTest, "pdsPrepareUploadFolderDirectory", "folder");

        /* execute */
        gitToTest.cleanGitDirectory();

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());

    }

    @Test
    void when_cleanGitDirectory_throws_exception_IOexception_is_thrown() throws IOException {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");
        ReflectionTestUtils.setField(gitToTest, "pdsPrepareUploadFolderDirectory", "folder");
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> gitToTest.cleanGitDirectory());

        /* test */
        assertTrue(exception.getMessage().contains("Error while cleaning git directory: folder"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "var1", "var2", "varXYZ" })
    void setEnvironmentVariables_returns_mocked_process(String var) throws IOException {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");
        ReflectionTestUtils.setField(gitToTest, "pdsPrepareUploadFolderDirectory", "folder");

        /* execute */
        gitToTest.setEnvironmentVariables("SOME_ENV", var);

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());

    }

    @Test
    void when_setEnvironmentVariables_throws_exception_IOexception_is_thrown() throws IOException {
        /* prepare */
        ReflectionTestUtils.setField(gitToTest, "username", "user");
        ReflectionTestUtils.setField(gitToTest, "password", "password");
        ReflectionTestUtils.setField(gitToTest, "pdsPrepareUploadFolderDirectory", "folder");
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> gitToTest.setEnvironmentVariables("KEY", "value"));

        /* test */
        assertTrue(exception.getMessage().contains("Error while exporting environment variable: KEY:value"));
    }
}