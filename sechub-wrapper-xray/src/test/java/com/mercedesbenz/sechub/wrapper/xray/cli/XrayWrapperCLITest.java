// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.wrapper.xray.util.EnvironmentVariableReader;

class XrayWrapperCLITest {

    XrayWrapperCLI cliToTest;

    @BeforeEach
    void beforeEach() {
        cliToTest = new XrayWrapperCLI();
    }

    @Test
    void start_valid_parameters() {
        /* prepare */
        try (MockedConstruction<XrayWrapperArtifactoryClientSupport> mockConstruction = mockConstruction(XrayWrapperArtifactoryClientSupport.class)) {
            String[] args = { "--name", "myname", "--checksum", "sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", "--scantype",
                    "docker", "--outputfile", "outfile" };
            final EnvironmentVariableReader[] lamdaReference = new EnvironmentVariableReader[1];
            try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
                lamdaReference[0] = mock;
                when(mock.readEnvAsString(any())).thenReturn("username");
            })) {

                /* execute */
                cliToTest.start(args);
            }
            verify(lamdaReference[0], times(2)).readEnvAsString(any());
        }
    }
}