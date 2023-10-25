package com.mercedesbenz.sechub.wrapper.xray.cli;

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
    void parseCommandLineArgs_valid_arguments() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--name", "myname", "--checksum", "sha256:123", "--scantype", "docker", "--outputfile", "outfile" };
        XrayWrapperCommandLineParser.Arguments arguments;

        /* execute */
        arguments = parser.parseCommandLineArgs(args);

        /* test */
        assertEquals("myname", arguments.name());
        assertEquals("123", arguments.checksum());
        assertEquals("latest", arguments.tag());
    }

    @Test
    void parseCommandLineArgs_missing_required_arguments() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--checksum", "sha256:123", "--scantype", "docker", "--outputfile", "outfile" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parser.parseCommandLineArgs(args));
    }

    @Test
    void parseCommandLineArgs_missing_all_arguments() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parser.parseCommandLineArgs(args));
    }

    @Test
    void parseCommandLineArgs_invalid_scanType_parameter() throws XrayWrapperCommandLineParserException {
        /* prepare */
        String[] args = { "--checksum", "sha256:123", "--scantype", "invalid", "--outputfile", "outfile" };

        /* execute + test */
        assertThrows(XrayWrapperCommandLineParserException.class, () -> parser.parseCommandLineArgs(args));
    }
}