// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

class XrayAPIAuthenticationHeaderTest {

    @Test
    void buildAuthHeader_with_valid_input() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(any())).thenReturn("username");
        })) {
            /* execute */
            String auth = XrayAPIAuthenticationHeader.buildBasicAuthHeader();

            /* test */
            assertEquals("Basic dXNlcm5hbWU6dXNlcm5hbWU=", auth);
        }
    }

    @Test
    void setAuthHeader_throws_xrayWrapperException() {
        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> XrayAPIAuthenticationHeader.buildBasicAuthHeader());

        /* test */
        assertEquals("Authentication not possible because of missing environment variables XRAY_USER and XRAY_PASSWORD", exception.getMessage());
        assertEquals(XrayWrapperExitCode.NOT_NULLABLE, exception.getExitCode());
    }
}