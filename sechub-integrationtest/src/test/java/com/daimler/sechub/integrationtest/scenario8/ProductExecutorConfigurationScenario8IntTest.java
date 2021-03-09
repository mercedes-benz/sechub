// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario8;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario8.Scenario8.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;
import com.daimler.sechub.integrationtest.api.AssertFullScanData;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestExecutorProductIdentifier;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class ProductExecutorConfigurationScenario8IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario8.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    @Test
    public void one_profile_with_two_configs_for_same_product_result_in_scanlog_with_separated_metadata_and_result_entries() {

        /* prepare */
        // info: we use NETSPARKER here, because our standard results for a web scan in mocked
        // product results for web scans is currently always NETSPARKER
        UUID config1UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER,"exec-config-1");
        UUID config2UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER,"exec-config-2");

        String profileId = "profile1";

        createOrResetProfile(profileId);

        /* @formatter:off */
        as(SUPER_ADMIN).
            addConfigurationToProfile(profileId,config1UUID,config2UUID).
            addProjectsToProfile(profileId, PROJECT_1);
        
        /* execute */
        ExecutionResult result = as(USER_1).createWebScanAndFetchScanData(PROJECT_1);
        
        /* test */
        File zipfile = as(SUPER_ADMIN).downloadFullScanDataFor(result.getSechubJobUUID());
        AssertFullScanData assertFullScanDataZipFile = assertFullScanDataZipFile(zipfile);
        
        assertFullScanDataZipFile.
            dumpDownloadFilePath().
            containsFile("SERECO.json"). //fallback means no config, so no postfix
            containsFile("metadata_SERECO.json").//fallback means no config, so no postfix

            containsFile("NETSPARKER_"+config1UUID+".xml").
            containsFile("metadata_NETSPARKER_"+config1UUID+".json").
            
            containsFile("NETSPARKER_"+config2UUID+".xml").
            containsFile("metadata_NETSPARKER_"+config2UUID+".json").
        
            containsFileStartingWith("log_").
            containsFiles(7);
        
        AdapterMetaData metaData1 = assertFullScanDataZipFile.resolveFile("metadata_NETSPARKER_"+config1UUID+".json").asAdapterMetaData();
        assertEquals("+1+1", metaData1.getValue(AbstractMockedAdapter.KEY_METADATA_REUSED));
        
        AdapterMetaData metaData2 = assertFullScanDataZipFile.resolveFile("metadata_NETSPARKER_"+config2UUID+".json").asAdapterMetaData();
        assertEquals("+1+1", metaData2.getValue(AbstractMockedAdapter.KEY_METADATA_REUSED));
      
        /* @formatter:on */
    }
    
    @Test
    public void two_profiles_with_two_configs_for_same_product_result_in_scanlog_with_separeted_metadata_and_result_entries() {

        /* prepare */
        // info: we use NETSPARKER here, because our standard results for a web scan in mocked
        // product results for web scans is currently always NETSPARKER
        UUID config1UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER,"exec-config-1");
        UUID config2UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER,"exec-config-2");

        String profileId1 = "profile1";

        createOrResetProfile(profileId1);

        /* @formatter:off */
        as(SUPER_ADMIN).
            addConfigurationToProfile(profileId1,config1UUID).
            addProjectsToProfile(profileId1, PROJECT_1);
        
        String profileId2 = "profile2";

        createOrResetProfile(profileId2);

        as(SUPER_ADMIN).
            addConfigurationToProfile(profileId1,config2UUID).
            addProjectsToProfile(profileId1, PROJECT_1);
        
        /* execute */
        ExecutionResult result = as(USER_1).createWebScanAndFetchScanData(PROJECT_1);
        
        /* test */
        File zipfile = as(SUPER_ADMIN).downloadFullScanDataFor(result.getSechubJobUUID());
        AssertFullScanData assertFullScanDataZipFile = assertFullScanDataZipFile(zipfile);
        assertFullScanDataZipFile.
            dumpDownloadFilePath().
            containsFile("SERECO.json"). //fallback means no config, so no postfix
            containsFile("metadata_SERECO.json").//fallback means no config, so no postfix

            containsFile("NETSPARKER_"+config1UUID+".xml").
            containsFile("metadata_NETSPARKER_"+config1UUID+".json").
            
            containsFile("NETSPARKER_"+config2UUID+".xml").
            containsFile("metadata_NETSPARKER_"+config2UUID+".json").
            containsFileStartingWith("log_").
            containsFiles(7);
        
        /* check adapter persistence of reused meta data not more than two times called */
        AdapterMetaData metaData1 = assertFullScanDataZipFile.resolveFile("metadata_NETSPARKER_"+config1UUID+".json").asAdapterMetaData();
        assertEquals("+1+1", metaData1.getValue(AbstractMockedAdapter.KEY_METADATA_REUSED));
        
        AdapterMetaData metaData2 = assertFullScanDataZipFile.resolveFile("metadata_NETSPARKER_"+config2UUID+".json").asAdapterMetaData();
        assertEquals("+1+1", metaData2.getValue(AbstractMockedAdapter.KEY_METADATA_REUSED));
       
        /* @formatter:on */
    }
    
    @Test
    public void two_profiles_with_same_config_inside_for_same_product_result_in_scanlog_with_one_metadata_and_result_entry_for_product() {

        /* prepare */
        // info: we use NETSPARKER here, because our standard results for a web scan in mocked
        // product results for web scans is currently always NETSPARKER
        UUID config1UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER,"exec-config-1");

        /* @formatter:off */
        String profileId1 = "profile1";

        createOrResetProfile(profileId1);

        as(SUPER_ADMIN).
            addConfigurationToProfile(profileId1,config1UUID).
            addProjectsToProfile(profileId1, PROJECT_1);
        
        String profileId2 = "profile2";

        createOrResetProfile(profileId2);

        as(SUPER_ADMIN).
            addConfigurationToProfile(profileId2,config1UUID).
            addProjectsToProfile(profileId2, PROJECT_1);
        
        /* execute */
        ExecutionResult result = as(USER_1).createWebScanAndFetchScanData(PROJECT_1);
        
        /* test */
        File zipfile = as(SUPER_ADMIN).downloadFullScanDataFor(result.getSechubJobUUID());
        AssertFullScanData assertFullScanDataZipFile = assertFullScanDataZipFile(zipfile);
        assertFullScanDataZipFile.
            dumpDownloadFilePath().
            containsFile("SERECO.json"). //fallback means no config, so no postfix
            containsFile("metadata_SERECO.json").//fallback means no config, so no postfix

            containsFile("NETSPARKER_"+config1UUID+".xml").
            
            containsFile("metadata_NETSPARKER_"+config1UUID+".json").
            containsFileStartingWith("log_").
            containsFiles(5);
        
        /* check adapter persistence of reused meta data not more than two times called */
        AdapterMetaData metaData = assertFullScanDataZipFile.resolveFile("metadata_NETSPARKER_"+config1UUID+".json").asAdapterMetaData();
        assertEquals("+1+1", metaData.getValue(AbstractMockedAdapter.KEY_METADATA_REUSED));
        /* @formatter:on */
    }

    private TestExecutionProfile createOrResetProfile(String profileId) {

        /* prepare */
        dropExecutionProfileIfExisting(profileId);

        TestExecutionProfile profile = new TestExecutionProfile();
        profile.enabled=true;
        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profile);
        return profile;
    }

    private UUID createExecutorConfig(TestExecutorProductIdentifier productIdentifier, String name) {
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = productIdentifier.name();
        config.name = name;
        config.executorVersion = 1;
        config.setup.baseURL = "https://baseurl.product.example.com/start";
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key1", "value1"));
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key2", "value2"));
        config.enabled=true;
        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);
        return uuid;
    }

}
