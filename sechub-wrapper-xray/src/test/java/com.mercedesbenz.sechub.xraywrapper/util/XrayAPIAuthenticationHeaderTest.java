package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayAPIAuthenticationHeader.buildAuthHeader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

class XrayAPIAuthenticationHeaderTest {

    @Test
    void buildAuthHeader_with_valid_input() {
        /* execute + test */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(any())).thenReturn("username");
        })) {
            String auth = buildAuthHeader();
            assertEquals("Basic dXNlcm5hbWU6dXNlcm5hbWU=", auth);
        }
    }

    @Test
    void setAuthHeader_throws_xrayWrapperRuntimeException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> buildAuthHeader());
    }
}