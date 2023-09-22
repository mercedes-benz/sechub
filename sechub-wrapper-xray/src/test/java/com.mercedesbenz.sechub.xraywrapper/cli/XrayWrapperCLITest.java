package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XrayWrapperCLITest {

    XrayWrapperCLI cli;

    @BeforeEach
    void beforeEach() throws IOException {
        cli = new XrayWrapperCLI();
    }

    @Test
    void test_start_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> cli.start(null));
    }

    @Test
    void test_start() {
        /* prepare */
        mockConstruction(XrayClientArtifactoryController.class);
        String[] args = { "--name", "myname", "--sha256", "sha256:xxx", "--scantype", "docker", "--outputfile", "outfile" };

        /* execute + test */
        cli.start(args);
    }
}