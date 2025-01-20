// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.integrationtest.SecurityTestHelper.TestTargetType;
import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
class SecurityTestHelperTest {

    private SecurityTestHelper helperToTest;

    @BeforeEach
    void beforeEach() throws Exception {
        helperToTest = new SecurityTestHelper(TestTargetType.SECHUB_SERVER, new URL("https://localhost"));
    }

    @ParameterizedTest
    @CsvSource({ "DHE-DSS-AES256-SHA, SHA", "SEED-SHA, SHA", "DHE-DSS-AES256-SHA256, SHA256", ", ", "DES-CBC-MD5, MD5" })
    void getMac_finds_expected_ciphers(String cipher, String expectedMAC) {
        /* prepare */
        CipherCheck check = new CipherCheck();
        check.cipher = cipher;

        /* execute */
        String result = helperToTest.getMac(check);

        /* test */
        assertEquals(expectedMAC, result, "MAC not as expected for cipher:" + cipher);
    }

}
