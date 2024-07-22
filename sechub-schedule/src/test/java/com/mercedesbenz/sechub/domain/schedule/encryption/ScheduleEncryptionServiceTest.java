// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubSecretKeyProviderFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class ScheduleEncryptionServiceTest {

    private ScheduleEncryptionService serviceToTest;
    private SecHubSecretKeyProviderFactory secHubSecretKeyProviderFactory;
    private EncryptionSupport encryptionSupport;
    private PersistentCipherFactory cipherFactory;
    private ScheduleEncryptionPoolFactory scheduleEncryptionPoolFactory;
    private ScheduleEncryptionPool scheduleEncryptionPool;
    private PersistentCipher fakedNoneCipher;
    private ScheduleLatestCipherPoolIdResolver scheduleLatestCipherPoolIdResolver;
    private ScheduleCipherPoolDataProvider poolDataProvider;
    private PersistentCipher fakedAES256Cipher;
    private DomainMessageService domainMessageService;

    @BeforeEach
    void beforeEach() throws Exception {
        serviceToTest = new ScheduleEncryptionService();

        secHubSecretKeyProviderFactory = mock(SecHubSecretKeyProviderFactory.class);
        when(secHubSecretKeyProviderFactory.createSecretKeyProvider(eq(SecHubCipherPasswordSourceType.NONE), any())).thenReturn(null);

        fakedNoneCipher = mock(PersistentCipher.class, "faked none cipher");
        fakedAES256Cipher = mock(PersistentCipher.class, "faked AES256 cipher");
        cipherFactory = mock(PersistentCipherFactory.class);

        when(cipherFactory.createCipher(null, PersistentCipherType.NONE)).thenReturn(fakedNoneCipher);
        when(cipherFactory.createCipher(null, PersistentCipherType.AES_GCM_SIV_256)).thenReturn(fakedAES256Cipher);

        encryptionSupport = mock(EncryptionSupport.class);

        domainMessageService = mock(DomainMessageService.class);

        scheduleEncryptionPoolFactory = mock(ScheduleEncryptionPoolFactory.class);
        scheduleEncryptionPool = mock(ScheduleEncryptionPool.class);
        scheduleLatestCipherPoolIdResolver = mock(ScheduleLatestCipherPoolIdResolver.class);
        poolDataProvider = mock(ScheduleCipherPoolDataProvider.class);

        serviceToTest.poolDataProvider = poolDataProvider;
        serviceToTest.encryptionSupport = encryptionSupport;
        serviceToTest.scheduleEncryptionPoolFactory = scheduleEncryptionPoolFactory;
        serviceToTest.scheduleLatestCipherPoolIdResolver = scheduleLatestCipherPoolIdResolver;
        serviceToTest.secretKeyProviderFactory = secHubSecretKeyProviderFactory;
        serviceToTest.cipherFactory = cipherFactory;
        serviceToTest.domainMessageService = domainMessageService;

        when(scheduleEncryptionPoolFactory.createEncryptionPool(any())).thenReturn(scheduleEncryptionPool);
    }

    @Test
    void application_started_triggers_encryption_pool_refresh_event() throws Exception {
        /* prepare */
        Long latestCipherPoolId = Long.valueOf(1);
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

        /* execute */
        serviceToTest.applicationStarted();

        /* test */
        assertEncryptionPoolInitEventSent();

    }

    @Test
    void createInitialCipherPoolData_working_as_expected() throws Exception {

        /* prepare */
        String testText = "testtext";

        SecHubEncryptionData data = new SecHubEncryptionData();
        data.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_256);
        data.setPasswordSourceType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        data.setPasswordSourceData("SECRET_1");

        byte[] initBytes = UUID.randomUUID().toString().getBytes();

        EncryptionResult result = mock(EncryptionResult.class);
        byte[] encryptedBytes = "faked-encryped-content".getBytes(Charset.forName("UTF-8"));
        when(result.getEncryptedData()).thenReturn(encryptedBytes);
        when(result.getInitialVector()).thenReturn(new InitializationVector(initBytes));

        when(encryptionSupport.encryptString(eq(testText), eq(fakedAES256Cipher))).thenReturn(result);
        when(encryptionSupport.decryptString(eq(encryptedBytes), eq(fakedAES256Cipher), eq(new InitializationVector(initBytes)))).thenReturn(testText);

        /* execute */
        ScheduleCipherPoolData createdPoolData = serviceToTest.createInitialCipherPoolData(data, testText);

        /* test */
        ArgumentCaptor<String> testTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(encryptionSupport).encryptString(testTextCaptor.capture(), eq(fakedAES256Cipher));

        String encryptedTestText = testTextCaptor.getValue();
        assertThat(encryptedTestText).isEqualTo(testText);

        assertThat(createdPoolData.getPasswordSourceType()).isEqualTo(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        assertThat(createdPoolData.getTestEncrypted()).isEqualTo(encryptedBytes);
        assertThat(createdPoolData.getPasswordSourceData()).isEqualTo("SECRET_1");
        assertThat(createdPoolData.getTestInitialVector()).isEqualTo(initBytes);
        assertThat(createdPoolData.getTestText()).isEqualTo(testText);
        assertThat(createdPoolData.getAlgorithm()).isEqualTo(SecHubCipherAlgorithm.AES_GCM_SIV_256);
        assertThat(createdPoolData.getCreatedFrom()).isNull();// the user is NOT set, as defined in JavaDoc of method
        assertThat(createdPoolData.getCreated()).isNotNull();

    }

    @Test
    void createInitialCipherPoolData_null_from_encryption_support_throws_exception() throws Exception {

        /* prepare */
        String testText = "testtext";

        SecHubEncryptionData data = new SecHubEncryptionData();
        data.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_256);
        data.setPasswordSourceType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        data.setPasswordSourceData("SECRET_1");

        byte[] initBytes = UUID.randomUUID().toString().getBytes();

        EncryptionResult result = mock(EncryptionResult.class);
        byte[] encryptedBytes = "faked-encryped-content".getBytes(Charset.forName("UTF-8"));
        when(result.getEncryptedData()).thenReturn(encryptedBytes);
        when(result.getInitialVector()).thenReturn(new InitializationVector(initBytes));

        when(encryptionSupport.encryptString(eq(testText), eq(fakedAES256Cipher))).thenReturn(result);
        when(encryptionSupport.decryptString(eq(encryptedBytes), eq(fakedAES256Cipher), eq(new InitializationVector(initBytes)))).thenReturn(null);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.createInitialCipherPoolData(data, testText)).isInstanceOf(ScheduleEncryptionException.class)
                .hasMessageContaining("decrypted value is null");

    }

    @Test
    void createInitialCipherPoolData_decrypted_test_text_not_as_origin_throws_exception() throws Exception {

        /* prepare */
        String testText = "testtext";

        SecHubEncryptionData data = new SecHubEncryptionData();
        data.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_256);
        data.setPasswordSourceType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        data.setPasswordSourceData("SECRET_1");

        byte[] initBytes = UUID.randomUUID().toString().getBytes();

        EncryptionResult result = mock(EncryptionResult.class);
        byte[] encryptedBytes = "faked-encryped-content".getBytes(Charset.forName("UTF-8"));
        when(result.getEncryptedData()).thenReturn(encryptedBytes);
        when(result.getInitialVector()).thenReturn(new InitializationVector(initBytes));

        when(encryptionSupport.encryptString(eq(testText), eq(fakedAES256Cipher))).thenReturn(result);
        when(encryptionSupport.decryptString(eq(encryptedBytes), eq(fakedAES256Cipher), eq(new InitializationVector(initBytes)))).thenReturn("wrong-result");

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.createInitialCipherPoolData(data, testText)).isInstanceOf(ScheduleEncryptionException.class)
                .hasMessageContaining("decrypted value is not origin test text");

    }

    @Test
    void refreshEncryptionPoolAndLatestIdIfNecessary_pooldata_provider_says_same_poolids_last_cipher_id_kept() throws Exception {

        /* prepare */
        when(scheduleEncryptionPool.getAllPoolIds()).thenReturn(Set.of(5L));

        // simulate originally setup done on startup;
        long latestCipherPoolId = 4L;
        serviceToTest.scheduleEncryptionPool = scheduleEncryptionPool;
        serviceToTest.latestCipherPoolId = latestCipherPoolId;// just other value;

        // important part: contains exactly ....
        when(poolDataProvider.isContainingExactlyGivenPoolIds(any())).thenReturn(true);

        /* execute */
        serviceToTest.refreshEncryptionPoolAndLatestPoolIdIfNecessary();

        /* test */
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(latestCipherPoolId); // still same
        assertNoDomainMessageEventSent();

    }

    @Test
    void refreshEncryptionPoolAndLatestIdIfNecessary_pooldata_provider_says_same_poolids_no_new_pool_created_or_used() throws Exception {

        /* prepare */
        when(scheduleEncryptionPool.getAllPoolIds()).thenReturn(Set.of(5L));

        // simulate originally setup done on startup;
        long latestCipherPoolId = 4L;
        serviceToTest.scheduleEncryptionPool = scheduleEncryptionPool;
        serviceToTest.latestCipherPoolId = latestCipherPoolId;// just other value;

        // important part: contains exactly ....
        when(poolDataProvider.isContainingExactlyGivenPoolIds(any())).thenReturn(true);

        /* execute */
        serviceToTest.refreshEncryptionPoolAndLatestPoolIdIfNecessary();

        /* test */
        verify(scheduleEncryptionPoolFactory, never()).createEncryptionPool(any()); // never a new pool is created!
        verify(scheduleEncryptionPool, never()).getCipherForPoolId(anyLong());
        assertThat(serviceToTest.scheduleEncryptionPool).isSameAs(scheduleEncryptionPool);// not changed
        assertNoDomainMessageEventSent();

    }

    @Test
    void refreshEncryptionPoolAndLatestIdIfNecessary_when_pool_ids_are_NOT_same_latest_cipher_pool_id_is_changed() throws Exception {

        /* prepare */
        ScheduleCipherPoolData poolData1 = mock(ScheduleCipherPoolData.class);
        when(poolData1.getId()).thenReturn(Long.valueOf(4));
        List<ScheduleCipherPoolData> poolDataList = List.of(poolData1);

        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(poolDataList);
        long oldCipherPoolId = 4L;
        when(scheduleEncryptionPoolFactory.createEncryptionPool(poolDataList)).thenReturn(scheduleEncryptionPool);

        long newCipherPoolId = 5L;
        when(scheduleEncryptionPool.getAllPoolIds()).thenReturn(Set.of(oldCipherPoolId, newCipherPoolId));
        when(scheduleEncryptionPool.getCipherForPoolId(newCipherPoolId)).thenReturn(fakedNoneCipher);
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(poolDataList)).thenReturn(newCipherPoolId);

        // simulate originally setup done on startup;
        serviceToTest.scheduleEncryptionPool = scheduleEncryptionPool;
        serviceToTest.latestCipherPoolId = oldCipherPoolId;

        // important part: contains exactly ....
        when(poolDataProvider.isContainingExactlyGivenPoolIds(any())).thenReturn(false);

        /* check preconditions */
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(oldCipherPoolId);

        /* execute */
        serviceToTest.refreshEncryptionPoolAndLatestPoolIdIfNecessary();

        /* test */
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(newCipherPoolId);

    }

    @Test
    void refreshEncryptionPoolAndLatestIdIfNecessary_when_pool_ids_are_NOT_same_new_pool_is_created_and_used() throws Exception {

        /* prepare */
        ScheduleCipherPoolData poolData1 = mock(ScheduleCipherPoolData.class);
        when(poolData1.getId()).thenReturn(Long.valueOf(4));
        List<ScheduleCipherPoolData> poolDataList = List.of(poolData1);

        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(poolDataList);
        long oldCipherPoolId = 4L;

        long newCipherPoolId = 5L;
        when(scheduleEncryptionPool.getAllPoolIds()).thenReturn(Set.of(oldCipherPoolId, newCipherPoolId));

        ScheduleEncryptionPool newEncryptionPool = mock(ScheduleEncryptionPool.class);
        when(scheduleEncryptionPoolFactory.createEncryptionPool(poolDataList)).thenReturn(newEncryptionPool);
        when(newEncryptionPool.getCipherForPoolId(newCipherPoolId)).thenReturn(fakedNoneCipher);
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(poolDataList)).thenReturn(newCipherPoolId);

        // simulate originally setup done on startup;
        serviceToTest.scheduleEncryptionPool = scheduleEncryptionPool;
        serviceToTest.latestCipherPoolId = oldCipherPoolId;

        // important part: contains exactly ....
        when(poolDataProvider.isContainingExactlyGivenPoolIds(any())).thenReturn(false);

        /* check preconditions */
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(oldCipherPoolId);

        /* execute */
        serviceToTest.refreshEncryptionPoolAndLatestPoolIdIfNecessary();

        /* test */
        verify(scheduleEncryptionPoolFactory).createEncryptionPool(poolDataList); // a new pool is created!
        assertThat(serviceToTest.scheduleEncryptionPool).isEqualTo(newEncryptionPool); // and set
        assertEncryptionPoolInitEventSent(); // event must be sent!

    }

    @Test
    void refreshEncryptionPoolAndLatestIdIfNecessary_when_pool_ids_are_NOT_but_new_pool_creation_fails_old_setup_is_kept() throws Exception {

        /* prepare */
        ScheduleCipherPoolData poolData1 = mock(ScheduleCipherPoolData.class);
        when(poolData1.getId()).thenReturn(Long.valueOf(4));
        List<ScheduleCipherPoolData> poolDataList = List.of(poolData1);

        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(poolDataList);
        long oldCipherPoolId = 4L;

        long newCipherPoolId = 5L;
        when(scheduleEncryptionPool.getAllPoolIds()).thenReturn(Set.of(oldCipherPoolId, newCipherPoolId));

        when(scheduleEncryptionPoolFactory.createEncryptionPool(poolDataList))
                .thenThrow(new ScheduleEncryptionException("was not able to create new encryption pool"));
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(poolDataList)).thenReturn(newCipherPoolId);

        // simulate originally setup done on startup;
        serviceToTest.scheduleEncryptionPool = scheduleEncryptionPool;
        serviceToTest.latestCipherPoolId = oldCipherPoolId;

        // important part: contains exactly ....
        when(poolDataProvider.isContainingExactlyGivenPoolIds(any())).thenReturn(false);

        /* check preconditions */
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(oldCipherPoolId);

        /* execute */
        serviceToTest.refreshEncryptionPoolAndLatestPoolIdIfNecessary();

        /* test */
        verify(scheduleEncryptionPoolFactory).createEncryptionPool(poolDataList); // it is tried to create a new pool
        assertThat(serviceToTest.latestCipherPoolId).isEqualTo(oldCipherPoolId); // not changed
        assertThat(serviceToTest.scheduleEncryptionPool).isEqualTo(scheduleEncryptionPool); // not changed

        assertNoDomainMessageEventSent(); // no event may be sent!

    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 1, 3270 })
    void applicationStarted_latest_cipher_pool_id_is_resolved(long value) throws Exception {

        /* prepare */
        Long latestCipherPoolId = Long.valueOf(value);
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

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
        when(scheduleEncryptionPool.getCipherForPoolId(latestPoolId)).thenReturn(null);
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestPoolId);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.applicationStarted()).isInstanceOf(IllegalStateException.class)
                .hasMessage("Encryption pool has no entry for latest cipher pool id: %d", latestPoolId);

    }

    @Test
    void applicationStarted_encryption_pool_is_created_by_factory_with_data_from_pooldataprovider() throws Exception {
        /* prepare */
        Long latestCipherPoolId = Long.valueOf(12);
        List<ScheduleCipherPoolData> list = new ArrayList<>();

        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);
        when(poolDataProvider.ensurePoolDataAvailable()).thenReturn(list);
        when(scheduleEncryptionPoolFactory.createEncryptionPool(list)).thenReturn(scheduleEncryptionPool);

        /* execute + test */
        serviceToTest.applicationStarted();

        verify(poolDataProvider).ensurePoolDataAvailable();
        verify(scheduleEncryptionPoolFactory).createEncryptionPool(list);
        verify(scheduleEncryptionPool).getCipherForPoolId(latestCipherPoolId); // the created pool is used
    }

    @Test
    void encryptWithLatestCipher_calls_encryptionsupport_with_latest_cipher_and_uses_result() throws Exception {
        /* prepare */
        String stringToEncrypt = "please-encrypt-me";
        Long latestCipherPoolId = Long.valueOf(12);

        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

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
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

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
        when(scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(anyList())).thenReturn(latestCipherPoolId);
        when(scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId)).thenReturn(fakedNoneCipher);

        // but we use here an "older" cipher pool entry
        when(scheduleEncryptionPool.getCipherForPoolId(usedCipherPoolId)).thenReturn(cipherForDecryption);

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

    void assertNoDomainMessageEventSent() {
        verify(domainMessageService, never()).sendAsynchron(any(DomainMessage.class));
    }

    void assertEncryptionPoolInitEventSent() {
        ArgumentCaptor<DomainMessage> captor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(domainMessageService).sendAsynchron(captor.capture());

        DomainMessage sentDomainMessage = captor.getValue();
        assertThat(sentDomainMessage.getMessageId()).isEqualTo(MessageID.SCHEDULE_ENCRYPTION_POOL_INITIALIZED);
    }
}