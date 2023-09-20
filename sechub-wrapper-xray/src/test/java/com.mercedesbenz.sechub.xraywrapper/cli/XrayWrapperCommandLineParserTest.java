package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XrayWrapperCommandLineParserTest {

    XrayWrapperCommandLineParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new XrayWrapperCommandLineParser();
    }

    @Test
    public void testDockerParseArguments() {
        // prepare
        String[] args = { "--name", "myname", "--sha256", "sha256:xxx", "--scantype", "docker", "--outputfile", "outfile" };
        XrayWrapperCommandLineParser.Arguments arguments;

        // execute
        arguments = parser.parseCommandLineArgs(args);

        // assert
        assertEquals("myname", arguments.name());
        assertEquals("xxx", arguments.sha256());
        assertEquals("latest", arguments.tag());
    }

    @Test
    public void testInvalidParseArguments() {
        // prepare
        String[] args = { "--sha256", "sha256:xxx", "--scantype", "docker", "--outputfile", "outfile" };
        XrayWrapperCommandLineParser.Arguments arguments;

        // execute
        arguments = parser.parseCommandLineArgs(args);

        // assert
        assertNull(arguments);
    }
}