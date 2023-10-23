package com.mercedesbenz.sechub.xraywrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperScanTypes;

class XrayWrapperArtifactTest {
    @Test
    void test_xrayArtifact() {
        /* prepare */
        XrayWrapperArtifact artifact;
        String name = "myartifact";
        String sha256 = "xxx";
        String tag = "1.0";
        XrayWrapperScanTypes type = XrayWrapperScanTypes.DOCKER;

        /* execute */
        artifact = new XrayWrapperArtifact(name, sha256, tag, type);

        /* test */
        assertEquals(name, artifact.getName());
        assertEquals(tag, artifact.getTag());
        assertEquals(type, artifact.getArtifactType());
        assertEquals(sha256, artifact.getSecureHash());
    }
}