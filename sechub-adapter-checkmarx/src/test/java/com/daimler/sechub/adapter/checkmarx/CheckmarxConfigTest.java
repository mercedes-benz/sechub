package com.daimler.sechub.adapter.checkmarx;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig.CheckmarxConfigBuilder;

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

    private CheckmarxConfigBuilder createBuilderWithMandatoryParamatersSet() {
        CheckmarxConfigBuilder builder = CheckmarxConfig.builder();
        builder.setUser("testuserId");
        builder.setPasswordOrAPIToken("testapitoken");
        builder.setProjectId("testprojectid");
        builder.setTeamIdForNewProjects("testteamid");
        return builder;
    }

}
