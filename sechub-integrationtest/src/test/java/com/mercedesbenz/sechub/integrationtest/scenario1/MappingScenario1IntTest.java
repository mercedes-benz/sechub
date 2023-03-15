// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingData;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingEntry;

public class MappingScenario1IntTest {
    private static final String TEST_UPDATE_MAPPING_ID = "sechub.integrationtest.mapping.updatecheck";
    private static final String TEST_NOT_EXISTING_MAPPING_ID = "sechub.integrationtest.mapping.doesnotexist";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Test
    public void update_mapping_is_possible() {
        /* prepare */

        MappingData mappingData = new MappingData();
        MappingEntry entry = new MappingEntry("pattern1", "replacement1", System.currentTimeMillis() + "");
        mappingData.getEntries().add(entry);

        /* execute */
        as(SUPER_ADMIN).updateMapping(TEST_UPDATE_MAPPING_ID, mappingData);

        /* test */
        assertUser(SUPER_ADMIN).canGetMapping(TEST_UPDATE_MAPPING_ID).contains(entry);

    }

    @Test
    public void fetching_mapping_with_id_not_existing_returns_empty_mockdata() {

        /* test */
        assertUser(SUPER_ADMIN).canGetMapping(TEST_NOT_EXISTING_MAPPING_ID).hasEntries(0);

    }

}
