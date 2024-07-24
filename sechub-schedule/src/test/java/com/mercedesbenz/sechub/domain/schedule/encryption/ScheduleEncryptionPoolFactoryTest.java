// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubSecretKeyProviderFactory;

class ScheduleEncryptionPoolFactoryTest {
    private ScheduleEncryptionPoolFactory factoryToTest;
    private PersistentCipherFactory cipherFactory;
    private EncryptionSupport encryptionSupport;
    private SecHubSecretKeyProviderFactory secHubSecretKeyProviderFactory;
    private SecretKeyProvider noneSecretKeyProvider;
    private InitializationVector noneInitVector1;
    private PersistentCipher noneCipher1;
    private byte[] noneInitVector1Bytes;;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new ScheduleEncryptionPoolFactory();

        cipherFactory = mock(PersistentCipherFactory.class);
        encryptionSupport = mock(EncryptionSupport.class);
        secHubSecretKeyProviderFactory = mock(SecHubSecretKeyProviderFactory.class);

        factoryToTest.cipherFactory = cipherFactory;
        factoryToTest.encryptionSupport = encryptionSupport;
        factoryToTest.secHubSecretKeyProviderFactory = secHubSecretKeyProviderFactory;

        noneSecretKeyProvider = mock(SecretKeyProvider.class, "none-secret-keyprovider");
        when(secHubSecretKeyProviderFactory.createSecretKeyProvider(PersistentCipherType.NONE, SecHubCipherPasswordSourceType.NONE, null))
                .thenReturn(noneSecretKeyProvider);

        noneCipher1 = mock(PersistentCipher.class, "none-cipher1");
        when(cipherFactory.createCipher(noneSecretKeyProvider, PersistentCipherType.NONE)).thenReturn(noneCipher1);
        when(noneCipher1.createNewInitializationVector()).thenReturn(noneInitVector1);
        noneInitVector1 = mock(InitializationVector.class);
        noneInitVector1Bytes = new byte[] {};
        when(noneInitVector1.getInitializationBytes()).thenReturn(noneInitVector1Bytes);

    }

    @Test
    void createEncryptionPool_null_throws_illegal_argument() throws Exception {

        assertThatThrownBy(() -> factoryToTest.createEncryptionPool(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("never be null");
    }

    @Test
    void createEncryptionPool_empty_map_is_accepted() throws Exception {

        /* execute */
        ScheduleEncryptionPool pool = factoryToTest.createEncryptionPool(Collections.emptyList());

        /* test */
        assertThat(pool).isNotNull();
        assertThat(pool.getCipherForPoolId(Long.valueOf(0))).isNull();

    }

    @Test
    void createEncryptionPool_map_with_valid_entry0_is_accepted() throws Exception {

        /* prepare */
        String plainText = "testdata-plaintext";
        byte[] noneEncrypted = plainText.getBytes(Charset.forName("UTF-8"));
        List<ScheduleCipherPoolData> list = new ArrayList<>();

        // create valid cipher pool data
        ScheduleCipherPoolData data1 = new ScheduleCipherPoolData();
        data1.algorithm = SecHubCipherAlgorithm.NONE;
        data1.secHubCipherPasswordSourceType = SecHubCipherPasswordSourceType.NONE;
        data1.created = LocalDateTime.now();
        data1.createdFrom = "user1";
        data1.id = Long.valueOf(0);
        data1.testEncrypted = noneEncrypted;
        data1.testText = plainText;
        data1.testInitialVector = noneInitVector1Bytes;

        list.add(data1);

        // simulate encryption - needed for internal validation
        when(encryptionSupport.decryptString(eq(noneEncrypted), eq(noneCipher1), any(InitializationVector.class))).thenReturn(plainText);

        /* execute */
        ScheduleEncryptionPool pool = factoryToTest.createEncryptionPool(list);

        /* test */
        assertThat(pool).isNotNull();
        assertThat(pool.getCipherForPoolId(Long.valueOf(0))).isSameAs(noneCipher1);

        ArgumentCaptor<InitializationVector> initVectorCaptor = ArgumentCaptor.forClass(InitializationVector.class);
        verify(encryptionSupport).decryptString(eq(noneEncrypted), eq(noneCipher1), initVectorCaptor.capture());

        InitializationVector usedVector = initVectorCaptor.getValue();
        assertThat(usedVector.getInitializationBytes()).isEqualTo(noneInitVector1Bytes);

    }

    @Test
    void createEncryptionPool_map_with_valid_entry0_but_wrong_decryption_is_NOT_accepted_throws_exception() throws Exception {

        /* prepare */
        String plainText = "testdata-plaintext";
        byte[] noneEncrypted = plainText.getBytes(Charset.forName("UTF-8"));
        List<ScheduleCipherPoolData> list = new ArrayList<>();

        // create valid cipher pool data
        ScheduleCipherPoolData data1 = new ScheduleCipherPoolData();
        data1.algorithm = SecHubCipherAlgorithm.NONE;
        data1.secHubCipherPasswordSourceType = SecHubCipherPasswordSourceType.NONE;
        data1.created = LocalDateTime.now();
        data1.createdFrom = "user1";
        data1.id = Long.valueOf(0);
        data1.testEncrypted = noneEncrypted;
        data1.testText = plainText;
        data1.testInitialVector = noneInitVector1Bytes;

        list.add(data1);

        // simulate encryption - needed for internal validation
        when(encryptionSupport.decryptString(eq(noneEncrypted), eq(noneCipher1), any(InitializationVector.class))).thenReturn("wrong decrypted");

        /* execute + test  @formatter:off */
        assertThatThrownBy(() -> factoryToTest.createEncryptionPool(list)).
                                    isInstanceOf(ScheduleEncryptionException.class).
                                    hasMessageContaining("cipher pool").
                                    hasMessageContaining("cannot be handled");
        /* @formatter:on */

    }

}
