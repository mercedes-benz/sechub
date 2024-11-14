// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.login.EncodingType;

class StringDecoderTest {

    private StringDecoder supportToTest = new StringDecoder();

    @Test
    void when_secret_key_is_null_an_exception_is_thrown() {
        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> supportToTest.decodeIfNecessary(null, EncodingType.AUTODETECT));

        assertEquals("The secret key must not be null!", exception.getMessage());
    }

    /* @formatter:off */
    @ParameterizedTest
    @ValueSource(strings = { "746573742d737472696e67",
            "  7465   7374 2d    73 7472 696e 67 ",
            "7465 7374 2D73 7472 696E 67",
            "746573742D737472696E67",
            "ORSXG5BNON2HE2LOM4======",
            "   ORSXG5     BNON2HE2L  OM4===== =  ",
            "dGVzdC1zdHJpbmc=", " dG    VzdC     1zdHJp bmc  ="
    })
    /* @formatter:on */
    void encoded_values_are_decoded_correctly_ignoring_spaces_or_tabs(String value) throws DecoderException {
        /* prepare */
        String expected = "test-string";

        /* execute */
        byte[] decoded = supportToTest.decodeIfNecessary(value, EncodingType.AUTODETECT);

        /* test */
        assertEquals(expected, new String(decoded));
    }

    @ParameterizedTest
    @ValueSource(strings = { "���", "NotEncoded!", "@:_DASF2daagjtz", "HelloWorld" })
    void not_encoded_values_are_correctly_treated_ignoring_spaces_or_tabs(String value) throws DecoderException {
        /* execute */
        byte[] decoded = supportToTest.decodeIfNecessary(value, EncodingType.AUTODETECT);

        /* test */
        assertEquals(value, new String(decoded));
    }

}
