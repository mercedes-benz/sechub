package com.mercedesbenz.sechub.xraywrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperScanTypes;

class XrayWrapperArtifactTest {
    @Test
    void xrayArtifact_create_valid_artifact() {
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
        assertEquals(sha256, artifact.getChecksum());
    }

    @Test
    void xrayArtifact_create_invalid_artifact() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> new XrayWrapperArtifact(null, null, null, null));
    }
}