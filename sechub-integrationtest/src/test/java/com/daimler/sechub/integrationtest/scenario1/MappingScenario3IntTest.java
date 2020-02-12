// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import static com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.integrationtest.scenario3.Scenario3;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

public class MappingScenario3IntTest {
    
    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Test
    public void mapping_for_checkmarx_preset_changed_by_administration_is_stored_in_scan_d()
            throws IOException {

        /* prepare */
        // cleanup former mapping
        changeScanMappingDirectly(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId());
        changeScanMappingDirectly(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId());
        triggerScanConfigRefresh();
        
        clearMetaDataInspection();

        assertInspections().hasAmountOfInspections(0);

        /* now prepare parameters for execution:*/
        MappingData mappingData1 = new MappingData();
        MappingEntry entry = new MappingEntry("scenario3_project1", "123456", "");
        mappingData1.getEntries().add(entry);
        
        MappingData mappingData2 = new MappingData();
        MappingEntry entry2 = new MappingEntry("scenario3_project1", "replacedTeamId", "");
        mappingData2.getEntries().add(entry2);

        /* @formatter:off */
        
        /* execute */
        as(SUPER_ADMIN).
            updateMapping(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(), mappingData1).
            updateMapping(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId(), mappingData2);
        
        /* test */
        triggerScanConfigRefresh(); // ensure loaded (is done periodically - we force execution here)
        
        ExecutionResult result = as(USER_1).withSecHubClient().
                startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUD();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);
        assertInspections().
            hasAmountOfInspections(1).
            inspectionNr(0).
                hasId("CHECKMARX").
                hasNotice("presetid","123456").// scenario3_project1 -> replacedPresetId
                hasNotice("teamid", "replacedTeamId");// scenario3_project1 -> replacedTeamId
        /* @formatter:on */
    }

}
