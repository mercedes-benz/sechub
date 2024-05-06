package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_PASSWORD;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_USERNAME;
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

class WrapperGitTest {

    WrapperGit gitToTest;

    PDSProcessAdapterFactory processAdapterFactory;

    ProcessAdapter processAdapter;

    JGitAdapter jGitAdapter;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        gitToTest = new WrapperGit();
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        processAdapter = mock(ProcessAdapter.class);
        jGitAdapter = mock(JGitAdapter.class);
        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(processAdapter.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);
        doNothing().when(jGitAdapter).clone(any());

        gitToTest.processAdapterFactory = processAdapterFactory;
        gitToTest.jGitAdapter = jGitAdapter;
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_cloneRepository_is_executed_the_processAdapterFactory_starts_JGit_clone(String location) throws IOException {
        /* prepare */
        Map<String, SealedObject> credentialMap = new HashMap<>();
        credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, CryptoAccess.CRYPTO_STRING.seal("user"));
        credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, CryptoAccess.CRYPTO_STRING.seal("password"));
        GitContext contextGit = (GitContext) new GitContext.GitContextBuilder().setCloneWithoutHistory(true).setLocation(location)
                .setCredentialMap(credentialMap).setUploadDirectory("folder").build();

        /* execute */
        gitToTest.downloadRemoteData(contextGit);

        /* test */
        verify(jGitAdapter, times(1)).clone(contextGit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void when_JGit_clone_throws_exception_RuntimeException_is_thrown(String location) {
        /* prepare */
        RuntimeException runtimeException = new RuntimeException("Error while cloning from repository: " + location);
        doThrow(runtimeException).when(jGitAdapter).clone(any());

        /* execute */
        RuntimeException exception = assertThrows(RuntimeException.class, () -> gitToTest.downloadRemoteData(any()));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cloning from repository: " + location));
    }

    @Test
    void when_cleanGitDirectory_is_executed_the_processAdapterFactory_starts_one_process() throws IOException {
        /* prepare */
        String directory = "test-upload-folder";

        /* execute */
        gitToTest.cleanUploadDirectory(directory);

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());

    }

    @Test
    void when_cleanGitDirectory_throws_exception_IOException_is_thrown() throws IOException {
        /* prepare */
        when(processAdapterFactory.startProcess(any())).thenThrow(IOException.class);
        String directory = "test-upload-folder";

        /* execute */
        Exception exception = assertThrows(Exception.class, () -> gitToTest.cleanUploadDirectory(directory));

        /* test */
        assertTrue(exception.getMessage().contains("Error while cleaning git directory: test-upload-folder"));
    }

}