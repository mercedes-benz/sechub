package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SealedObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
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
    void getRepositoryURL_returns_repositoryURL_with_username_and_password_from_ENV(String location) {
        /* execute */
        String repositoryUrl = gitToTest.getRepositoryURL(location);

        /* test */
        assertTrue(repositoryUrl.contains("https://$PDS_PREPARE_CREDENTIAL_USERNAME:$PDS_PREPARE_CREDENTIAL_PASSWORD@"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_cloneRepository_is_executed_the_processAdapterFactory_starts_one_process(String location) throws IOException {
        /* prepare */
        Map<String, SealedObject> credentialMap = new HashMap<>();
        credentialMap.put("username", CryptoAccess.CRYPTO_STRING.seal("user"));
        credentialMap.put("password", CryptoAccess.CRYPTO_STRING.seal("password"));
        GitContext gitContext = new GitContext(location, true, credentialMap, "folder");

        /* execute */
        gitToTest.cloneRepository(gitContext);

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_startProcess_throws_exception_IOException_is_thrown(String location) throws IOException {
        /* prepare */
        Map<String, SealedObject> credentialMap = new HashMap<>();
        credentialMap.put("username", CryptoAccess.CRYPTO_STRING.seal("user"));
        credentialMap.put("password", CryptoAccess.CRYPTO_STRING.seal("password"));
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);
        GitContext gitContext = new GitContext(location, true, credentialMap, "folder");

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> gitToTest.cloneRepository(gitContext));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cloning repository: "));
    }

    @Test
    void when_cleanGitDirectory_is_executed_the_processAdapterFactory_starts_one_process() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";

        /* execute */
        gitToTest.cleanGitDirectory(directory);

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());

    }

    @Test
    void when_cleanGitDirectory_throws_exception_IOException_is_thrown() throws IOException {
        /* prepare */
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);
        String directory = "test-upload-folder";

        /* execute */
        Exception exception = assertThrows(Exception.class, () -> gitToTest.cleanGitDirectory(directory));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cleaning git directory: test-upload-folder"));
    }
}