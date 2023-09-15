package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;

class XrayWrapperCommandLineParserTest {

    XrayWrapperCommandLineParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new XrayWrapperCommandLineParser();
    }

    @Test
    public void testParseDockerArguments() {
        // prepare
        String[] args = { "--image", "myimage:1.6", "--sha256", "sha256:xxx" };
        XrayDockerImage image;

        // execute
        image = parser.parseDockerArguments(args);

        // assert
        assertEquals("myimage", image.getDocker_name());
        assertEquals("1.6", image.getDocker_tag());
        assertEquals("xxx", image.getSHA256());
    }

    @Test
    public void testLatestParseDockerArguments() {
        // prepare
        String[] args = { "--image", "myimage", "--sha256", "sha256:xxx" };
        XrayDockerImage image;

        // execute
        image = parser.parseDockerArguments(args);

        // assert
        assertEquals("latest", image.getDocker_tag());
    }

    @Test
    public void testInvalidParseDockerArguments() {
        // prepare
        String[] args = { "-unkownParam", "myimage" };
        XrayDockerImage image;

        // execute
        image = parser.parseDockerArguments(args);

        // assert
        assertNull(image);
    }
}