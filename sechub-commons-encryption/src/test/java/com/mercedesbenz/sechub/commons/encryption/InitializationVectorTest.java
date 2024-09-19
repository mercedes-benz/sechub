// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InitializationVectorTest {

    @Test
    void null_bytes() throws Exception {

        /* execute */
        InitializationVector initialVectorToTest = new InitializationVector(null);

        /* test */
        assertThat(initialVectorToTest.getInitializationBytes()).isNull();

    }

    @Test
    void filled_bytes() throws Exception {

        /* execute */
        InitializationVector initialVectorToTest = new InitializationVector("filled-bytes...".getBytes());

        /* test */
        assertThat(initialVectorToTest.getInitializationBytes()).isEqualTo("filled-bytes...".getBytes());

    }

}
