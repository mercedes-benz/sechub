package com.mercedesbenz.sechub.pds.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;

class PDSEncryptionServiceTest {

    private PDSEncryptionConfiguration configuration;
    private PDSEncryptionService serviceToTest;
    private PersistentCipherFactory cipherFactory;
    private EncryptionSupport encryptionSupport;

    @BeforeEach
    public void beforeEach() throws Exception {
        configuration = mock(PDSEncryptionConfiguration.class);
        cipherFactory = mock(PersistentCipherFactory.class);
        encryptionSupport = mock(EncryptionSupport.class);

        serviceToTest = new PDSEncryptionService();

        serviceToTest.configuration = configuration;
        serviceToTest.cipherFactory = cipherFactory;
        serviceToTest.encryptionSupport = encryptionSupport;
    }

    @ParameterizedTest
    @EnumSource(PDSCipherAlgorithm.class)
    void init_creates_expected_cipher_with_secret_key_inside(PDSCipherAlgorithm algorithm) throws Exception {

        /* prepare */
        String testSecretPlainText = "test-secret";

        when(configuration.getAlgorithm()).thenReturn(algorithm);
        String base64String = Base64.getEncoder().encodeToString(testSecretPlainText.getBytes());
        when(configuration.getSecretKeyBytes()).thenReturn(base64String.getBytes());

        PersistentCipher cipher = mock(PersistentCipher.class);
        when(cipherFactory.createCipher(any(), eq(algorithm.getType()))).thenReturn(cipher);

        /* execute */
        serviceToTest.init();

        /* test */
        assertThat(serviceToTest.cipher).isEqualTo(cipher);
        ArgumentCaptor<SecretKeyProvider> captor = ArgumentCaptor.forClass(SecretKeyProvider.class);
        verify(cipherFactory).createCipher(captor.capture(), eq(algorithm.getType()));

        SecretKeyProvider capturedProvider = captor.getValue();
        switch (algorithm) {
        case NONE:
            assertThat(capturedProvider).isNull(); // no secret key provider for NONE
            break;
        default:
            assertThat(capturedProvider.getSecretKey().getEncoded()).isEqualTo(testSecretPlainText.getBytes());
            break;

        }

    }

    @Test
    void decryptString_returns_decrypted_string_by_encryption_support() throws Exception {

        /* prepare */

        InitializationVector initialVector = mock(InitializationVector.class);
        byte[] encryptedData = "encrypted-data".getBytes();

        PersistentCipher cipher = mock(PersistentCipher.class);
        when(cipherFactory.createCipher(any(), any())).thenReturn(cipher);
        PDSCipherAlgorithm algorithm = mock(PDSCipherAlgorithm.class);
        when(configuration.getAlgorithm()).thenReturn(algorithm);

        serviceToTest.init();

        String resultFromEncryptionSupport = "encrypted test data from encryption support...";
        when(encryptionSupport.decryptString(encryptedData, cipher, initialVector)).thenReturn(resultFromEncryptionSupport);

        /* execute */
        String result = serviceToTest.decryptString(encryptedData, initialVector);

        /* test */
        assertThat(result).isEqualTo(resultFromEncryptionSupport);

    }

}
