package com.mercedesbenz.sechub.xraywrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class XrayArtifactTest {
    @Test
    public void test_xrayArtifact() {
        /* prepare */
        XrayArtifact artifact;
        String name = "myartifact";
        String sha256 = "xxx";
        String tag = "1.0";
        String type = "docker";

        /* execute */
        artifact = new XrayArtifact(name, sha256, tag, type);

        /* test */
        assertEquals(name, artifact.getName());
        assertEquals(tag, artifact.getTag());
        assertEquals(type, artifact.getArtifactType());
        assertEquals(sha256, artifact.getSha256());
    }
}