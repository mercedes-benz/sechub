package com.mercedesbenz.sechub.xraywrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

class XrayConfigurationCreaterTest {

    @Test
    public void testXrayConfigurationCreater() {
        // prepare
        XrayConfiguration xrayConfiguration;

        // execute
        xrayConfiguration = XrayConfigurationCreater.createXrayConfiguration("docker", "output");

        // assert
        assertEquals("docker", xrayConfiguration.getScan_type());
        assertEquals("output", xrayConfiguration.getSecHubReport());
    }

}