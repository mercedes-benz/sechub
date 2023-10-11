package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayAPIAuthenticationHeader.buildAuthHeader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class XrayAPIAuthenticationHeaderTest {

    @Test
    public void test_setAuthHeader() {
        /* prepare */
        String s = "string";

        /* execute + test */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(s)).thenReturn("username");
        })) {
            String auth = buildAuthHeader();
            assertEquals("Basic bnVsbDpudWxs", auth);
        }
        ;

    }
}