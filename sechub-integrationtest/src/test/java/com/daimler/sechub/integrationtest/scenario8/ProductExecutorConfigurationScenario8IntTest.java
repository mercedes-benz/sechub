// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario8;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario8.Scenario8.*;

import java.io.File;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

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
    public void one_profile_with_two_configs_for_same_product_result_in_scanlog_with_separeted_metadata_and_result_entries() {

        /* prepare */
        // info: we use NETSPARKER here, because our standard results for a web scan in mocked
        // product results for web scans is currently always NETSPARKER
        UUID config1UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER);
        UUID config2UUID = createExecutorConfig(TestExecutorProductIdentifier.NETSPARKER);

        String profileId = "profile1";

        createOrResetProfile(profileId);

        /* @formatter:off */
        as(SUPER_ADMIN).
            addConfigurationToProfile(profileId,config1UUID,config2UUID).
            addProjectsToProfile(profileId, PROJECT_1);
        
        /* execute */
        ExecutionResult result = as(USER_1).createWebScanAndFetchScanData(PROJECT_1);
        
        /* test */
        File zipfile = as(SUPER_ADMIN).downloadFullScanDataFor(result.getSechubJobUUD());
        assertFullScanDataZipFile(zipfile).
            dumpDownloadFilePath().
            containsFile("SERECO-default.json").
            containsFile("metadata_SERECO-default.json").

            containsFile("NETSPARKER-"+config1UUID+".xml").
            containsFile("metadata_NETSPARKER-"+config1UUID+".json").
            
            containsFile("NETSPARKER-"+config2UUID+".xml").
            containsFile("metadata_NETSPARKER-"+config2UUID+".json");
        
        /* @formatter:on */
    }

    private TestExecutionProfile createOrResetProfile(String profileId) {

        /* prepare */
        dropExecutionProfileIfExisting(profileId);

        TestExecutionProfile profile = new TestExecutionProfile();

        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profile);
        return profile;
    }

    private UUID createExecutorConfig(TestExecutorProductIdentifier productIdentifier) {
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = productIdentifier.name();
        config.name = "config name";
        config.executorVersion = 1;
        config.setup.baseURL = "https://baseurl.product.example.com/start";
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key1", "value1"));
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key2", "value2"));

        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);
        return uuid;
    }

}
