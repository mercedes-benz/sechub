package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XrayWrapperCommandLineParserTest {

    XrayWrapperCommandLineParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new XrayWrapperCommandLineParser();
    }

    @Test
    void test_parseCommandLineArgs() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--name", "myname", "--sha256", "sha256:123", "--scantype", "docker", "--outputfile", "outfile" };
        XrayWrapperCommandLineParser.Arguments arguments;

        /* execute */
        arguments = parser.parseCommandLineArgs(args);

        /* test */
        assertEquals("myname", arguments.name());
        assertEquals("123", arguments.sha256());
        assertEquals("latest", arguments.tag());
    }

    @Test
    void test_parseCommandLineArgs_invalid() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--sha256", "sha256:123", "--scantype", "docker", "--outputfile", "outfile" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parser.parseCommandLineArgs(args));
    }

    @Test
    void test_parseCommandLineArgs_empty() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parser.parseCommandLineArgs(args));
    }

    @Test
    void test_parseCommandLineArgs_invalidScanType() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--sha256", "sha256:123", "--scantype", "invalid", "--outputfile", "outfile" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parser.parseCommandLineArgs(args));
    }
}