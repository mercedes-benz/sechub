package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;

class ScheduleEncryptionServiceTest {

    private ScheduleEncryptionService serviceToTest;
    private SecretKeyProviderFactory secretKeyProviderFactory;
    private EncryptionSupport encryptionSupport;
    private PersistentCipherFactory cipherFactory;
    private EncryptionPoolFactory encryptionPoolFactory;
    private EncryptionPool encryptionPool;
    private PersistentCipher fakedNoneCipher;
    private LatestCipherPoolIdResolver latestCipherPoolIdResolver;
    private ScheduleCipherPoolDataProvider poolDataProvider;

    @BeforeEach
    void beforeEach() throws Exception {
        serviceToTest = new ScheduleEncryptionService();

        secretKeyProviderFactory = mock(SecretKeyProviderFactory.class);
        when(secretKeyProviderFactory.createSecretKeyProvider(eq(CipherPasswordSourceType.NONE), any())).thenReturn(null);

        fakedNoneCipher = mock(PersistentCipher.class, "faked none cipher");
        cipherFactory = mock(PersistentCipherFactory.class);
        when(cipherFactory.createCipher(null, PersistentCipherType.NONE)).thenReturn(fakedNoneCipher);

        encryptionSupport = mock(EncryptionSupport.class);

        encryptionPoolFactory = mock(EncryptionPoolFactory.class);
        encryptionPool = mock(EncryptionPool.class);
        latestCipherPoolIdResolver = mock(LatestCipherPoolIdResolver.class);
        poolDataProvider = mock(ScheduleCipherPoolDataProvider.class);

        serviceToTest.poolDataProvider = poolDataProvider;
        serviceToTest.encryptionSupport = encryptionSupport;
        serviceToTest.encryptionPoolFactory = encryptionPoolFactory;
        serviceToTest.latestCipherPoolIdResolver = latestCipherPoolIdResolver;

        when(encryptionPoolFactory.createEncryptionPool(any())).thenReturn(encryptionPool);
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 1, 3270 })
    void applicationStarted_latest_cipher_pool_id_is_resolved(long value) throws Exception {

        /* prepare */
        Long latestCipherPoolId = Long.valueOf(value);
        when(latestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(encryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

        /* execute */
        serviceToTest.applicationStarted();

        /* test */
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(latestCipherPoolId);

    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 1, 3270 })
    void applicationStarted_encryption_pool_has_not_latest_pool_id_inside_throws_illegal_state(long value) {
        /* prepare */
        Long latestPoolId = Long.valueOf(value);
        when(encryptionPool.getCipherForPoolId(latestPoolId)).thenReturn(null);
        when(latestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestPoolId);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.applicationStarted()).isInstanceOf(IllegalStateException.class)
                .hasMessage("Encryption pool has no entry for latest cipher pool id: %d", latestPoolId);

    }

    @Test
    void applicationStarted_encryption_pool_is_created_by_factory_with_data_from_pooldataprovider() throws Exception {
        /* prepare */
        Long latestCipherPoolId = Long.valueOf(12);
        List<ScheduleCipherPoolData> list = new ArrayList<>();

        when(latestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(encryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(encryptionPoolFactory.createEncryptionPool(list)).thenReturn(encryptionPool);

        /* execute + test */
        serviceToTest.applicationStarted();

        verify(poolDataProvider).ensurePoolDataAvailable();
        verify(encryptionPoolFactory).createEncryptionPool(list);
        verify(encryptionPool).getCipherForPoolId(latestCipherPoolId); // the created pool is used
    }

    @Test
    void encryptWithLatestCipher_calls_encryptionsupport_with_latest_cipher_and_uses_result() throws Exception {
        /* prepare */
        String stringToEncrypt = "please-encrypt-me";
        Long latestCipherPoolId = Long.valueOf(12);

        when(latestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(encryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

        // fake encryption by cipher
        EncryptionResult encryptionResultFromSupport = mock(EncryptionResult.class);
        byte[] encryptedData = "i-am-encrypted".getBytes();
        InitializationVector initialVector = mock(InitializationVector.class);
        when(encryptionResultFromSupport.getEncryptedData()).thenReturn(encryptedData);
        when(encryptionResultFromSupport.getInitialVector()).thenReturn(initialVector);

        when(encryptionSupport.encryptString(stringToEncrypt, fakedNoneCipher)).thenReturn(encryptionResultFromSupport);

        // simulate spring startup - necessary to get pool filled.
        serviceToTest.applicationStarted();

        /* execute */
        ScheduleEncryptionResult result = serviceToTest.encryptWithLatestCipher(stringToEncrypt);

        /* test */
        verify(encryptionSupport).encryptString(stringToEncrypt, fakedNoneCipher);

        assertThat(result.getCipherPoolId()).isEqualTo(latestCipherPoolId);
        assertThat(result.getEncryptedData()).isEqualTo(encryptedData);
        assertThat(result.getInitialVector()).isEqualTo(initialVector);

    }

    @Test
    void decrypt_with_latest_cipher_for_decryption() throws Exception {
        /* prepare */
        byte[] encryptedData = "i-am-encrypted".getBytes();
        String decryptedString = "please-encrypt-me";
        Long latestCipherPoolId = Long.valueOf(12);
        Long usedCipherPoolId = latestCipherPoolId;
        PersistentCipher cipherForDecryption = fakedNoneCipher;

        // necessary to get startup not failing for missing latest pool id:
        when(latestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(encryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

        // fake encryption by cipher
        EncryptionResult encryptionResultFromSupport = mock(EncryptionResult.class);
        InitializationVector initialVector = mock(InitializationVector.class);
        when(encryptionResultFromSupport.getEncryptedData()).thenReturn(encryptedData);
        when(encryptionResultFromSupport.getInitialVector()).thenReturn(initialVector);

        when(encryptionSupport.decryptString(encryptedData, cipherForDecryption, initialVector)).thenReturn(decryptedString);

        // simulate spring startup - necessary to get pool filled.
        serviceToTest.applicationStarted();

        /* execute */
        String decrypted = serviceToTest.decryptToString(encryptedData, usedCipherPoolId, initialVector);

        /* test */
        verify(encryptionSupport).decryptString(encryptedData, cipherForDecryption, initialVector);
        assertThat(decrypted).isEqualTo(decryptedString);

    }

    @Test
    void decrypt_with_other_cipher_for_decryption() throws Exception {
        /* prepare */
        byte[] encryptedData = "i-am-encrypted".getBytes();
        String decryptedString = "please-encrypt-me";
        Long latestCipherPoolId = Long.valueOf(12);
        Long usedCipherPoolId = Long.valueOf(1);
        PersistentCipher cipherForDecryption = mock(PersistentCipher.class, "cipher for decryption");

        // necessary to get startup not failing for missing latest pool id:
        when(latestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(encryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

        // but we use here an "older" cipher pool entry
        when(encryptionPool.getCipherForPoolId(usedCipherPoolId)).thenReturn(cipherForDecryption);

        // fake encryption by cipher
        EncryptionResult encryptionResultFromSupport = mock(EncryptionResult.class);
        InitializationVector initialVector = mock(InitializationVector.class);
        when(encryptionResultFromSupport.getEncryptedData()).thenReturn(encryptedData);
        when(encryptionResultFromSupport.getInitialVector()).thenReturn(initialVector);

        when(encryptionSupport.decryptString(encryptedData, cipherForDecryption, initialVector)).thenReturn(decryptedString);

        // simulate spring startup - necessary to get pool filled.
        serviceToTest.applicationStarted();

        /* execute */
        String decrypted = serviceToTest.decryptToString(encryptedData, usedCipherPoolId, initialVector);

        /* test */
        verify(encryptionSupport).decryptString(encryptedData, cipherForDecryption, initialVector);
        assertThat(decrypted).isEqualTo(decryptedString);

    }
}