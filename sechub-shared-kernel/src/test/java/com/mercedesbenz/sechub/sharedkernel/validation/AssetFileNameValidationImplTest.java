// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AssetFileNameValidationImplTest {

    private AssetFileNameValidationImpl validation;

    @BeforeEach
    void beforeEach() {
        validation = new AssetFileNameValidationImpl();
    }

    @ParameterizedTest
    @ValueSource(strings = { "asset1.txt", "prod_local_config.txt" })
    void valid_filenames_accepted(String validFileName) {

        assertThat(validation.validate(validFileName).isValid()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "$asset1.txt" })
    void invalid_filenames_not_accepted(String validFileName) {

        assertThat(validation.validate(validFileName).isValid()).isFalse();
    }

}
