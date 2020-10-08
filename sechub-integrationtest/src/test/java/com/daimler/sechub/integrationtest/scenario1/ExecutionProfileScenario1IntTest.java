// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.integrationtest.api.ExecutionConstants;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileList;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileListEntry;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class ExecutionProfileScenario1IntTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionProfileScenario1IntTest.class);

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Test
    public void administrator_can_fetch_execution_profile_list_and_list_contains_default_profile_1() {
        /* execute */
        TestExecutionProfileList profiles = as(SUPER_ADMIN).fetchProductExecutionProfiles();

        /* test */
        for (TestExecutionProfileListEntry profileEntry : profiles.executionProfiles) {
            if (profileEntry.id.equals(ExecutionConstants.DEFAULT_PROFILE_1_ID)) {
                return;
            }
        }
        fail("Default profile not found! Found only profiles:" + JSONConverter.get().toJSON(profiles));
    }

    @Test
    public void sanity_check_for_default_profile1() {
        /* execute */
        TestExecutionProfile profile = as(SUPER_ADMIN).fetchProductExecutionProfile(ExecutionConstants.DEFAULT_PROFILE_1_ID);

        /* dump */
        String json = JSONConverter.get().toJSON(profile);
        LOG.info("default profile 1 as json:{}", json);

        /* test */
        assertEquals("default profile1 MUST have exactly 3 configurations active!", 3, profile.configurations.size());
        for (TestExecutorConfig config : profile.configurations) {
            assertTrue("config:" + config.name + " must be enabled but isn't!", config.enabled);
        }
        assertTrue("default profile1 must be enabled!!! Maybe a test has accidently change the profile? default profiles MAY NOT be altered!", profile.enabled);
    }

}
