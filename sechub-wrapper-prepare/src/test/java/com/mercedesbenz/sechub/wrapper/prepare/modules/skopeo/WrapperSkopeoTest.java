package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_PASSWORD;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_USERNAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SealedObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

class WrapperSkopeoTest {

    WrapperSkopeo wrapperToTest;
    PDSProcessAdapterFactory processAdapterFactory;
    ProcessAdapter processAdapter;

    private Path testPath = Path.of("folder");

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        wrapperToTest = new WrapperSkopeo();
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        processAdapter = mock(ProcessAdapter.class);
        when(processAdapterFactory.startProcess(any())).thenReturn(processAdapter);
        when(processAdapter.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(true);

        wrapperToTest.processAdapterFactory = processAdapterFactory;
    }

    @Test
    void when_download_is_executed_download_process_is_executed() throws IOException {
        /* prepare */
        SkopeoContext context = new SkopeoContext();
        context.setLocation("docker://ubuntu:22.04");
        context.setWorkingDirectory(testPath);

        /* execute */
        assertDoesNotThrow(() -> wrapperToTest.download(context));

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());
    }

    @Test
    void when_download_is_executed_with_credentials_download_and_login_process_are_executed() throws IOException {
        /* prepare */
        Map<String, SealedObject> credentialMap = new HashMap<String, SealedObject>();
        credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, CryptoAccess.CRYPTO_STRING.seal("username"));
        credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, CryptoAccess.CRYPTO_STRING.seal("password"));
        SkopeoContext context = new SkopeoContext();
        context.setLocation("docker://ubuntu:22.04");
        context.setWorkingDirectory(testPath);
        context.setCredentialMap(credentialMap);

        /* execute */
        assertDoesNotThrow(() -> wrapperToTest.download(context));

        /* test */
        verify(processAdapterFactory, times(2)).startProcess(any());
    }

    @Test
    void when_process_throws_exception_then_download_throws_exception() throws IOException {
        /* prepare */
        String location = "docker://ubuntu:22.04";
        SkopeoContext context = new SkopeoContext();
        context.setLocation(location);
        context.setWorkingDirectory(testPath);
        when(processAdapterFactory.startProcess(any())).thenThrow(new IOException());

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> wrapperToTest.download(context));

        /* test */
        assertEquals("Error while download with Skopeo from: " + location, exception.getMessage());

    }

    @Test
    void when_cleanUploadDirectory_is_executed_clean_process_is_executed() throws IOException {
        /* execute */
        assertDoesNotThrow(() -> wrapperToTest.cleanUploadDirectory(testPath));

        /* test */
        verify(processAdapterFactory, times(1)).startProcess(any());
    }

}