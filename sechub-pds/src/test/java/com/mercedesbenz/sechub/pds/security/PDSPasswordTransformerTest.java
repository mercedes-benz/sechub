// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PDSPasswordTransformerTest {

    private PDSPasswordTransformer transformerToTest;

    @BeforeEach
    void beforeEach() {
        transformerToTest = new PDSPasswordTransformer();
    }

    @Test
    void transform_password_without_noop() {
        /* prepare */
        String originPassword = "my-pwd";

        /* execute */
        String transformedPassword = transformerToTest.transformPassword(originPassword);

        /* test */
        assertEquals("{noop}my-pwd", transformedPassword);
    }

    @Test
    void skip_password_with_noop() {
        /* prepare */
        String originPassword = "{noop}my-pwd";

        /* execute */
        String transformedPassword = transformerToTest.transformPassword(originPassword);

        /* test */
        assertEquals("{noop}my-pwd", transformedPassword);
    }

    @Test
    void skip_password_with_bcrypt() {
        /* prepare */
        String originPassword = "{bcrypt}my-pwd";

        /* execute */
        String transformedPassword = transformerToTest.transformPassword(originPassword);

        /* test */
        assertEquals("{bcrypt}my-pwd", transformedPassword);
    }

    @Test
    void null_argument() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> {
            transformerToTest.transformPassword(null);
        });
    }
}