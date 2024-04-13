// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;

class XrayWrapperArtifactTest {
    @Test
    void xrayArtifact_create_valid_artifact() {
        /* prepare */
        String name = "myartifact";
        String sha256 = "xxx";
        String tag = "1.0";
        XrayWrapperScanTypes type = XrayWrapperScanTypes.DOCKER;

        /* execute */
        XrayWrapperArtifact artifact = new XrayWrapperArtifact(name, sha256, tag, type);

        /* test */
        assertEquals(name, artifact.getName());
        assertEquals(tag, artifact.getTag());
        Assertions.assertEquals(type, artifact.getArtifactType());
        assertEquals(sha256, artifact.getChecksum());
    }
}