// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig.CheckmarxConfigBuilder;

public class CheckmarxConfigTest {

    @Test
    public void builder_creates_config_with_default_client_secret_when_not_set() {
        /* prepare */
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();

        /* execute */
        CheckmarxConfig config = builder.build();

        /* test */
        assertEquals(CheckmarxConfig.DEFAULT_CLIENT_SECRET, config.getClientSecret());
    }

    @Test
    public void builder_creates_config_with_given_client_secret_when_set() {
        /* prepare */
        String newSecret = "test-secret";
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();
        builder.setClientSecret(newSecret);

        /* execute */
        CheckmarxConfig config = builder.build();

        /* test */
        assertEquals(newSecret, config.getClientSecret());
    }

    @Test
    public void builder_creates_config_with_engine_configuration_name_set() {
        /* prepare */
        String engineConfigurationName = "test-configuration";
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();
        builder.setEngineConfigurationName(engineConfigurationName);

        /* execute */
        CheckmarxConfig config = builder.build();

        /* test */
        assertEquals(engineConfigurationName, config.getEngineConfigurationName());
    }

    @Test
    public void builder_creates_config_with_default_engine_configuration_name() {
        /* prepare */
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();

        /* execute */
        CheckmarxConfig config = builder.build();

        /* test */
        assertEquals(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME, config.getEngineConfigurationName());
    }

    private CheckmarxConfigBuilder createBuilderWithMandatoryParamatersSet() {
        CheckmarxConfigBuilder builder = CheckmarxConfig.builder();
        builder.setUser("testuserId");
        builder.setPasswordOrAPIToken("testapitoken");
        builder.setProjectId("testprojectid");
        builder.setTeamIdForNewProjects("testteamid");
        return builder;
    }

}
