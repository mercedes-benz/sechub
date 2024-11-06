// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.test.TestFileReader;

public class AssetScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Before
    public void before() {

    }

    @Test
    public void asset_crud_test() {
        File uploadedFile = new File("./src/test/resources/asset/asset1.txt");
        
        as(SUPER_ADMIN).uploadAssetFile("my-asset-id", uploadedFile);
        
        File downloadedAssetFile =  as(SUPER_ADMIN).downloadAssetFile("my-asset-id", uploadedFile.getName());
        
        String output = TestFileReader.readTextFromFile(downloadedAssetFile);
        assertThat(output).isEqualTo("I am textfile \"asset1.txt\"");
        
        
        String checksum = TestAPI.createSHA256Of(uploadedFile);
        File downloadedAssetFileChecksum =  as(SUPER_ADMIN).downloadAssetFile("my-asset-id", uploadedFile.getName()+CommonConstants.DOT_CHECKSUM);
        String checksumDownloadd = TestFileReader.readTextFromFile(downloadedAssetFileChecksum);
        
        assertThat(checksumDownloadd).isEqualTo(checksum);
    }

}
