// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.integrationtest.api.ExecutionConstants;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileList;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileListEntry;

public class ExecutionProfileScenario1IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

	@Test
	public void administrator_can_fetch_execution_profile_list_and_list_contains_default_profile_1() {
		/* execute */
		TestExecutionProfileList profiles = as(SUPER_ADMIN).fetchProductExecutionProfiles();

		/* test*/
		for (TestExecutionProfileListEntry profileEntry: profiles.executionProfiles) {
		    if (profileEntry.id.equals(ExecutionConstants.DEFAULT_PROFILE_1_ID)) {
		        return;
		    }
		}
		fail("Default profile not found! Found only profiles:"+JSONConverter.get().toJSON(profiles));
	}


}
