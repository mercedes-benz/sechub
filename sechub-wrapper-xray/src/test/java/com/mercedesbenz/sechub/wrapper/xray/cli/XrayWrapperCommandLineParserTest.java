package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayWrapperCommandLineParserTest {

    XrayWrapperCommandLineParser parserToTest;

    @BeforeEach
    void beforeEach() {
        parserToTest = new XrayWrapperCommandLineParser();
    }

    @Test
    void parseCommandLineArgs_valid_arguments() throws XrayWrapperCommandLineParserException, XrayWrapperException {
        /* prepare */
        String[] args = { "--name", "myname", "--checksum", "sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", "--scantype", "docker",
                "--outputfile", "outfile" };

        /* execute */
        XrayWrapperCommandLineParser.Arguments arguments = parserToTest.parseCommandLineArgs(args);

        /* test */
        assertEquals("myname", arguments.name());
        assertEquals("5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", arguments.checksum());
        assertEquals("latest", arguments.tag());
    }

    @Test
    void parseCommandLineArgs_missing_required_arguments() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--checksum", "sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", "--scantype", "docker", "--outputfile",
                "outfile" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parserToTest.parseCommandLineArgs(args));
    }

    @Test
    void parseCommandLineArgs_missing_all_arguments() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parserToTest.parseCommandLineArgs(args));
    }

    @Test
    void parseCommandLineArgs_invalid_scanType_parameter() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--checksum", "sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", "--scantype", "invalid", "--outputfile",
                "outfile" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parserToTest.parseCommandLineArgs(args));
    }
}