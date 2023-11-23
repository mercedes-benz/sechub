package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

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
    void start_with_null_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> cliToTest.start(null));
    }

    @Test
    void start_valid_parameters() {
        /* prepare */
        MockedConstruction<XrayWrapperArtifactoryClientSupport> mockConstruction = mockConstruction(XrayWrapperArtifactoryClientSupport.class);
        String[] args = { "--name", "myname", "--checksum", "sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", "--scantype", "docker",
                "--outputfile", "outfile" };

        /* execute + test */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(any())).thenReturn("username");
        })) {
            cliToTest.start(args);
        }
        mockConstruction.close();
    }
}