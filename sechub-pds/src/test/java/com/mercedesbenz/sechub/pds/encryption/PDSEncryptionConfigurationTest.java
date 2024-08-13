// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.encryption;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PDSEncryptionConfigurationTest {

    private PDSEncryptionConfiguration configurationToTest;

    @BeforeEach
    void beforeEach() {
        configurationToTest = new PDSEncryptionConfiguration();
    }

    @ParameterizedTest
    @EnumSource(value = PDSCipherAlgorithm.class, mode = Mode.EXCLUDE, names = "NONE")
    void init_with_empty_key_throws_exception(PDSCipherAlgorithm algorithm) throws Exception {

        /* prepare */
        configurationToTest.algorithmAsString = algorithm.name();
        configurationToTest.secretKeyAsString = "";

        /* execute + test  @formatter:off */
        assertThatThrownBy(() ->
            configurationToTest.init()).
                isInstanceOf(PDSEncryptionException.class).
                hasMessageContaining(algorithm+" does not allow an empty secret key");

        /* @formatter:on */

    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void init_with_invalid_key_throws_NO_exception_for_NONE_algorithm(String invalidSecretKey) throws Exception {

        /* prepare */
        configurationToTest.algorithmAsString = PDSCipherAlgorithm.NONE.name();
        configurationToTest.secretKeyAsString = invalidSecretKey;

        /* execute + test  @formatter:off */
        assertThatNoException().isThrownBy(() ->
            configurationToTest.init());
        /* @formatter:on */

    }

    @ParameterizedTest
    @EnumSource(PDSCipherAlgorithm.class)
    void init_resets_secret_key_string_and_setup_internal_data(PDSCipherAlgorithm algorithm) throws Exception {

        /* prepare */
        configurationToTest.algorithmAsString = algorithm.name();
        configurationToTest.secretKeyAsString = "test-secret";

        /* execute */
        configurationToTest.init();

        /* test */
        assertThat(configurationToTest.secretKeyAsString).isNull(); // reset is done
        assertThat(configurationToTest.algorithmAsString).isNotNull();

        assertThat(configurationToTest.getAlgorithm()).isEqualTo(algorithm);
        assertThat(configurationToTest.getSecretKeyBytes()).isEqualTo("test-secret".getBytes());

    }

    @ParameterizedTest
    @EnumSource(PDSCipherAlgorithm.class)
    void init_resets_secret_key_string_and_setup_internal_data_lowercase_works_as_well(PDSCipherAlgorithm algorithm) throws Exception {

        /* prepare */
        configurationToTest.algorithmAsString = algorithm.name().toLowerCase();
        configurationToTest.secretKeyAsString = "test-secret";

        /* execute */
        configurationToTest.init();

        /* test */
        assertThat(configurationToTest.secretKeyAsString).isNull(); // reset is done
        assertThat(configurationToTest.algorithmAsString).isNotNull();

        assertThat(configurationToTest.getAlgorithm()).isEqualTo(algorithm);
        assertThat(configurationToTest.getSecretKeyBytes()).isEqualTo("test-secret".getBytes());

    }

    @ParameterizedTest
    @ValueSource(strings = "unknown")
    @EmptySource
    @NullSource
    void init_unknown_algorithm_throws_exception(String wrongAlgorithmAsText) throws Exception {

        /* prepare */
        configurationToTest.algorithmAsString = wrongAlgorithmAsText;
        configurationToTest.secretKeyAsString = "test-secret";

        /* execute + test */
        assertThatThrownBy(() -> configurationToTest.init()).isInstanceOf(PDSEncryptionException.class).hasMessageContaining("not supported");

        /* test */
        assertThat(configurationToTest.secretKeyAsString).isNull(); // reset is still done

    }

}
