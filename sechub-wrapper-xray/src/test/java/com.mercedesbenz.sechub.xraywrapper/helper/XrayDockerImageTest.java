package com.mercedesbenz.sechub.xraywrapper.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class XrayDockerImageTest {

    @Test
    public void testXrayDockerImage() {
        // prepare
        String name = "testimage";
        String tag = "2.0";
        String sha256 = "fhd9832fnwoifh932hdk";

        // execute
        XrayDockerImage image = new XrayDockerImage(name, tag, sha256);

        // assert
        assertEquals(name, image.getDocker_name());
        assertEquals(tag, image.getDocker_tag());
        assertEquals(sha256, image.getSHA256());
    }
}