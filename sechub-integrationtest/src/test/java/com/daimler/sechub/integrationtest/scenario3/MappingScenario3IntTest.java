// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultExecutorConfigurations;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;
import static com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultExecutorConfigurations.*;
public class MappingScenario3IntTest {
    
    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    /* @formatter:off */
    @Test
    public void checkmarx_executor_config_mapping_for_checkmarx_preset_changed_is_used_in_next_checkmarx_scan()
            throws IOException {

        /* prepare */
        String projectId = PROJECT_1.getProjectId();

        /* add new parameters before default for execution:*/
        MappingData teamIdMapping = new MappingData();
        List<MappingEntry> teamIdMappingEntries = teamIdMapping.getEntries();
        teamIdMappingEntries.add(new MappingEntry(projectId, "replacedTeamId", ""));
        teamIdMappingEntries.add(IntegrationTestDefaultExecutorConfigurations.CHECKMARX_TEAMID_MAPPING_DEFAULT_MAPPING);
        
        MappingData presetMapping = new MappingData();
        List<MappingEntry> presetIdMappingEntries = presetMapping.getEntries();
        presetIdMappingEntries.add(new MappingEntry(projectId, "123456", ""));
        presetIdMappingEntries.add(IntegrationTestDefaultExecutorConfigurations.CHECKMARX_PRESETID_MAPPING_DEFAULT_MAPPING);
        
        /* execute */
        as(SUPER_ADMIN).
            changeProductExecutorJobParameter(CHECKMARX_V1,MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId(),teamIdMapping.toJSON()).
            changeProductExecutorJobParameter(CHECKMARX_V1,MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(),presetMapping.toJSON());
        
        clearMetaDataInspection();
        assertInspections().hasAmountOfInspections(0);

        /* test */
        ExecutionResult result = as(USER_1).withSecHubClient().
                startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);
        assertInspections().
            hasAmountOfInspections(1).
            inspectionNr(0).
                hasId("CHECKMARX").
                hasNotice("presetid","123456").// scenario3_project1 -> replacedPresetId
                hasNotice("teamid", "replacedTeamId");// scenario3_project1 -> replacedTeamId
        /* @formatter:on */
    }
    
    @Test
    public void mapping_for_checkmarx_preset_template_cannot_be_changed_anoymous()
            throws IOException {

        /* prepare */

        MappingData mappingData1 = new MappingData();
        MappingEntry entry = new MappingEntry("scenario3_project1", "123456", "");
        mappingData1.getEntries().add(entry);

        /* @formatter:off */
        expectHttpFailure(()->{
            
            /* execute */
            as(ANONYMOUS).
                updateMapping(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(), mappingData1);
        }        
        , HttpStatus.UNAUTHORIZED);
        /* @formatter:on */
    }
    
    @Test
    public void mapping_for_checkmarx_preset_template_cannot_be_changed_by_user1_scenario3()
            throws IOException {

        /* prepare */

        MappingData mappingData1 = new MappingData();
        MappingEntry entry = new MappingEntry("scenario3_project1", "123456", "");
        mappingData1.getEntries().add(entry);

        /* @formatter:off */
        expectHttpFailure(()->{
            
            /* execute */
            as(USER_1).
                updateMapping(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(), mappingData1);
        }        
        , HttpStatus.FORBIDDEN);
        /* @formatter:on */
    }

}
