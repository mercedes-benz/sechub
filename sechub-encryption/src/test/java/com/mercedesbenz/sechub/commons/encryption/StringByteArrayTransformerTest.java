// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class StringByteArrayTransformerTest {

    private StringByteArrayTransformer transformerToTest;

    @BeforeEach
    void beforeEach() {
        transformerToTest = new StringByteArrayTransformer();
    }

    /* @formatter:off */
    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings= {
            "hello world",
            "🍐🍌🍓🍉",
            "Hello 🦄",
            "🥦🥕🥔🫘🥒🫑🌽🍆",
            "Äpfel sind grün"
    })
    /* @formatter:on */
    void transformFromBytes_bytes_are_transformed_back_via_utf_8(String plainText) {
        /* prepare */
        byte[] bytes = plainText == null ? null : plainText.getBytes(Charset.forName("UTF-8"));

        /* execute */
        String result = transformerToTest.transformFromBytes(bytes);

        /* test */
        assertThat(result).isEqualTo(plainText);
    }

    /* @formatter:off */
    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings= {
            "hello world",
            "🍐🍌🍓🍉",
            "Hello 🦄",
            "🥦🥕🥔🫘🥒🫑🌽🍆",
            "Äpfel sind grün"
    })
    /* @formatter:on */
    void transformToBytes(String plainText) {
        /* prepare */

        /* execute */
        Object result = transformerToTest.transformToBytes(plainText);

        /* test */
        byte[] expectedBytes = plainText == null ? null : plainText.getBytes(Charset.forName("UTF-8"));
        assertThat(result).isEqualTo(expectedBytes);
    }

}
