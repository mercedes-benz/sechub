// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecretValidatorProxySettingsTest {

    private SecretValidatorProxySettings proxyToTest;

    @BeforeEach
    void beforeEach() {
        proxyToTest = new SecretValidatorProxySettings();
    }

    @Test
    void sealing_and_unsealing_username_and_password_for_proxy_works_as_expected() {
        /* prepare */
        String user = "user";
        String password = "password";

        /* execute */
        proxyToTest.setUsername(user);
        proxyToTest.setPassword(password);

        /* test */
        assertEquals(user, proxyToTest.getUsername());
        assertEquals(password, proxyToTest.getPassword());
    }

    @Test
    void sealing_and_unsealing_null_as_username_and_password_for_proxy_works_as_expected() {
        /* execute */
        proxyToTest.setUsername(null);
        proxyToTest.setPassword(null);

        /* test */
        assertNull(proxyToTest.getUsername());
        assertNull(proxyToTest.getPassword());
    }

}
