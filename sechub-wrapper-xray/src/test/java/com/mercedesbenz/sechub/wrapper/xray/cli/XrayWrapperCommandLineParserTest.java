// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.junit.jupiter.api.Assertions.*;

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

        /* execute */
        XrayWrapperCommandLineParserException exception = assertThrows(XrayWrapperCommandLineParserException.class,
                () -> parserToTest.parseCommandLineArgs(args));

        /* test */
        assertEquals(
                "Required parameters were empty: [--checksum, sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc, --scantype, docker, --outputfile, outfile]",
                exception.getMessage());
    }

    @Test
    void parseCommandLineArgs_missing_all_arguments() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "" };

        /* execute */
        XrayWrapperCommandLineParserException exception = assertThrows(XrayWrapperCommandLineParserException.class,
                () -> parserToTest.parseCommandLineArgs(args));

        /* test */
        assertEquals("Could not parse parameters:[]", exception.getMessage());
    }

    @Test
    void parseCommandLineArgs_invalid_scanType_parameter() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--name", "myname", "--checksum", "sha256:5bfba04ea0d437b9d579f4978ffa0f81008e77abf875f38933fb56af845c7ddc", "--scantype", "invalid",
                "--outputfile", "outfile" };

        /* execute */
        XrayWrapperCommandLineParserException exception = assertThrows(XrayWrapperCommandLineParserException.class,
                () -> parserToTest.parseCommandLineArgs(args));

        /* test */
        assertTrue(exception.getMessage().contains("Scan type not supported: invalid"));
    }

    @Test
    void parseCommandLineArgs_invalid_sha256_parameter() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--name", "myname", "--checksum", "sha256:123", "--scantype", "docker", "--outputfile", "outfile" };

        /* execute */
        XrayWrapperCommandLineParserException exception = assertThrows(XrayWrapperCommandLineParserException.class,
                () -> parserToTest.parseCommandLineArgs(args));

        /* test */
        assertEquals("Checksum is not valid: 123", exception.getMessage());
    }
}