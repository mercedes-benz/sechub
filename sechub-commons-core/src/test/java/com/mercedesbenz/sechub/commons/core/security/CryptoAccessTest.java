// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security;

import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SealedObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CryptoAccessTest {

    @ParameterizedTest
    @ValueSource(strings = { "alpha", "gamma", "$12344" })
    @NullSource
    @EmptySource
    void seal_unseal_works(String data) {
        /* prepare */
        CryptoAccess<String> accessToTest = new CryptoAccess<>();

        /* execute */
        SealedObject sealedObject = accessToTest.seal(data);

        /* test */
        assertEquals(data, accessToTest.unseal(sealedObject));
    }

    @Test
    void multiple_unseal_possible() {
        /* prepare */
        CryptoAccess<String> accessToTest = new CryptoAccess<>();
        String content = "data1";

        SealedObject obj = accessToTest.seal(content);

        /* execute */
        assertEquals(content, accessToTest.unseal(obj));
        assertEquals(content, accessToTest.unseal(obj));
        assertEquals(content, accessToTest.unseal(obj));
        assertEquals(content, accessToTest.unseal(obj));
        assertEquals(content, accessToTest.unseal(obj));

    }

}
