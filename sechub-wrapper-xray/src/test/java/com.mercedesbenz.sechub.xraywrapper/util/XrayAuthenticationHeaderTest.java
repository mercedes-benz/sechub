package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayAuthenticationHeader.setAuthHeader;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class XrayAuthenticationHeaderTest {

    @Test
    public void testSetAuthHeader() {
        // prepare
        String auth;

        // execute
        auth = setAuthHeader();

        // assert
        assertTrue(auth.contains("Basic"));
    }
}