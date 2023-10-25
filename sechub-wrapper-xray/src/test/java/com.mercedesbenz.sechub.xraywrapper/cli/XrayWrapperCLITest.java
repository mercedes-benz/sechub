package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XrayWrapperCLITest {

    XrayWrapperCLI cli;

    @BeforeEach
    void beforeEach() {
        cli = new XrayWrapperCLI();
    }

    @Test
    void start_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> cli.start(null));
    }

    @Test
    void start_valid_parameters() {
        /* prepare */
        mockConstruction(XrayWrapperArtifactoryClientController.class);
        String[] args = { "--name", "myname", "--checksum", "sha256:xxx", "--scantype", "docker", "--outputfile", "outfile" };

        /* execute + test */
        cli.start(args);
    }
}