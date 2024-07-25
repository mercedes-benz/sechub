// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;

class SecHubSecretKeyProviderFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubSecretKeyProviderFactoryTest.class);

    private SecHubSecretKeyProviderFactory factoryToTest;
    private EncryptionEnvironmentEntryProvider encryptionEnvironmentEntryProvider;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new SecHubSecretKeyProviderFactory();
        encryptionEnvironmentEntryProvider = mock(EncryptionEnvironmentEntryProvider.class);

        factoryToTest.encryptionEnvironmentEntryProvider = encryptionEnvironmentEntryProvider;

    }

    @ParameterizedTest
    @EnumSource(SecHubCipherPasswordSourceType.class)
    void none_cipher_no_encryptionEnvironmentEntryProvider_used_no_matter_which_pwd_source_type_defined(SecHubCipherPasswordSourceType type) {
        /* execute */
        SecretKeyProvider result = factoryToTest.createSecretKeyProvider(PersistentCipherType.NONE, SecHubCipherPasswordSourceType.NONE, "SECRET_1");

        /* test */
        assertThat(result).isNull();

    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "ZW52aXJvbm1lbnQtZW50cnktcGxhaW4tdGV4dA==" }) // valid base 64
    void cipher_aes_256_with_none_source_type_is_not_supported(String sourceData) {
        /* @formatter:off */
        assertThatThrownBy(()->factoryToTest.createSecretKeyProvider(PersistentCipherType.AES_GCM_SIV_256, SecHubCipherPasswordSourceType.NONE, sourceData)).
            isInstanceOf(SecHubSecretKeyProviderFactoryException.class).
            hasMessage("Was not able to create key provider for cipherType: 'AES_GCM_SIV_256', passwordSourceType: 'NONE', cipherPasswordSourceData: '%s'", sourceData).
            hasRootCauseExactlyInstanceOf(IllegalArgumentException.class).
            hasRootCauseMessage("Password source type 'NONE' for cipher type: 'AES_GCM_SIV_256' is not supported!");
        /* @formatter:on */
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "ZW52aXJvbm1lbnQtZW50cnktcGxhaW4tdGV4dA==" }) // valid base 64
    void cipher_aes_128_with_none_source_type_is_not_supported(String sourceData) {
        /* @formatter:off */
        assertThatThrownBy(()->factoryToTest.createSecretKeyProvider(PersistentCipherType.AES_GCM_SIV_128, SecHubCipherPasswordSourceType.NONE, sourceData)).
        isInstanceOf(SecHubSecretKeyProviderFactoryException.class).
        hasMessage("Was not able to create key provider for cipherType: 'AES_GCM_SIV_128', passwordSourceType: 'NONE', cipherPasswordSourceData: '%s'", sourceData).
        hasRootCauseExactlyInstanceOf(IllegalArgumentException.class).
        hasRootCauseMessage("Password source type 'NONE' for cipher type: 'AES_GCM_SIV_128' is not supported!");
        /* @formatter:on */
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void when_environment_variable_not_defined_exception_is_thrown(String value) throws Exception {

        /* prepare */
        when(encryptionEnvironmentEntryProvider.getBase64EncodedEnvironmentEntry("SECRET_1")).thenReturn(value);

        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> factoryToTest.createSecretKeyProvider(PersistentCipherType.AES_GCM_SIV_256,
                SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE, "SECRET_1")).
            isInstanceOf(SecHubSecretKeyProviderFactoryException.class).
            hasMessage("Was not able to create key provider for cipherType: 'AES_GCM_SIV_256', passwordSourceType: 'ENVIRONMENT_VARIABLE', cipherPasswordSourceData: 'SECRET_1'").
            hasRootCauseExactlyInstanceOf(IllegalArgumentException.class).
            hasRootCauseMessage("The environment variable: SECRET_1 has no value!");
        /* @formatter:on */

    }

    @ParameterizedTest
    @ValueSource(strings = { "a$lbert", "1:2", " ZW52aXJvbm1lbnQtZW50cnktcGxhaW4tdGV4dA==" })
    void when_environment_variable_has_not_base_64_encoded_value_exception_is_thrown(String value) throws Exception {

        /* prepare */
        when(encryptionEnvironmentEntryProvider.getBase64EncodedEnvironmentEntry("SECRET_2")).thenReturn(value);

        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> factoryToTest.createSecretKeyProvider(PersistentCipherType.AES_GCM_SIV_128, SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE, "SECRET_2")).
            isInstanceOf(SecHubSecretKeyProviderFactoryException.class).
            hasMessage("Was not able to create key provider for cipherType: 'AES_GCM_SIV_128', passwordSourceType: 'ENVIRONMENT_VARIABLE', cipherPasswordSourceData: 'SECRET_2'").
            hasRootCauseExactlyInstanceOf(IllegalArgumentException.class);
        /* @formatter:on */

    }

    @ParameterizedTest
    @ValueSource(strings = { "ZW52aXJvbm1lbnQtZW50cnktcGxhaW4tdGV4dA== ", "ZW52aXJvbm1lbnQtZW50cnktcGxhaW4tdGV4dA==  ",
            "ZW52aXJvbm1lbnQtZW50cnktcGxhaW4tdGV4dA==\t" })
    void when_environment_variable_base64_encoded_value_but_value_ends_with_whitspace_exception_is_thrown(String value) throws Exception {

        /* prepare */
        when(encryptionEnvironmentEntryProvider.getBase64EncodedEnvironmentEntry("SECRET_3")).thenReturn(value);

        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> factoryToTest.createSecretKeyProvider(PersistentCipherType.AES_GCM_SIV_256, SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE, "SECRET_3")).
            isInstanceOf(SecHubSecretKeyProviderFactoryException.class).
            hasMessage("Was not able to create key provider for cipherType: 'AES_GCM_SIV_256', passwordSourceType: 'ENVIRONMENT_VARIABLE', cipherPasswordSourceData: 'SECRET_3'").
            hasRootCauseExactlyInstanceOf(IllegalArgumentException.class).
            hasRootCauseMessage("Input byte array has incorrect ending byte at 40");
        /* @formatter:on */

    }

    @Test
    void when_environment_variable_is_base_64_provider_has_secret_key_with_base_64_decrypted_data_inside() throws Exception {

        /* prepare */
        String plainTextAsString = "environment-entry-plain-text2";
        byte[] plainTextAsBytes = plainTextAsString.getBytes();
        String base64String = Base64.getEncoder().encodeToString(plainTextAsBytes);
        LOG.info("base64String:" + base64String);
        when(encryptionEnvironmentEntryProvider.getBase64EncodedEnvironmentEntry("SECRET_1")).thenReturn(base64String);

        /* execute */
        SecretKeyProvider result = factoryToTest.createSecretKeyProvider(PersistentCipherType.AES_GCM_SIV_256,
                SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE, "SECRET_1");

        /* test */
        assertThat(result).isNotNull();
        byte[] encoded = result.getSecretKey().getEncoded();
        assertThat(encoded).isEqualTo(plainTextAsBytes);

    }

}
