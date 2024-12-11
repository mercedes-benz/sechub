// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;
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
    public void asset_crud_operation_working_as_expected() {
        /* ------- */
        /* prepare */
        /* ------- */
        File uploadedFile1 = new File("./src/test/resources/asset/examples-1/asset1.txt");
        File uploadedFile2 = new File("./src/test/resources/asset/examples-1/asset2.txt");

        String assetId = "crud" + UUID.randomUUID().toString();

        /* --------------- */
        /* execute + test */
        /* --------------- */
        as(SUPER_ADMIN).uploadAssetFiles(assetId, uploadedFile1, uploadedFile2);

        // fetch all asset ids
        List<String> allAssetIds = as(SUPER_ADMIN).fetchAllAssetIds();
        assertThat(allAssetIds).contains(assetId);

        /* download files */
        File downloadedAssetFile1 = as(SUPER_ADMIN).downloadAssetFile(assetId, uploadedFile1.getName());

        String output = TestFileReader.readTextFromFile(downloadedAssetFile1);
        assertThat(output).isEqualTo("I am text file \"asset1.txt\"");

        /* fetch asset details and check content is as expected */
        AssetDetailData detailData = as(SUPER_ADMIN).fetchAssetDetails(assetId);
        assertThat(detailData.getAssetId()).isEqualTo(assetId);

        String checksum1 = TestAPI.createSHA256Of(uploadedFile1);
        AssetFileData expectedInfo1 = new AssetFileData();
        expectedInfo1.setChecksum(checksum1);
        expectedInfo1.setFileName("asset1.txt");

        String checksum2 = TestAPI.createSHA256Of(uploadedFile2);
        AssetFileData expectedInfo2 = new AssetFileData();
        expectedInfo2.setChecksum(checksum2);
        expectedInfo2.setFileName("asset2.txt");

        assertThat(detailData.getFiles()).contains(expectedInfo1, expectedInfo2).hasSize(2);

        /* delete single file from asset */
        as(SUPER_ADMIN).deleteAssetFile(assetId, "asset1.txt");

        /* check asset still exists in list and details contain only asset2.txt */
        assertThat(as(SUPER_ADMIN).fetchAllAssetIds()).contains(assetId);
        assertThat(as(SUPER_ADMIN).fetchAssetDetails(assetId).getFiles()).containsOnly(expectedInfo2);

        /*
         * Upload asset 2 again, but with different content - we use other file from
         * examples-2 instead of examples-1. Will override existing asset file.
         */
        File uploadedFile2changed = new File("./src/test/resources/asset/examples-2/asset2.txt");
        String checksum2changed = TestAPI.createSHA256Of(uploadedFile2changed);
        assertThat(checksum2changed).as("precondition-check that files are different").isNotEqualTo(checksum2);

        as(SUPER_ADMIN).uploadAssetFile(assetId, uploadedFile2changed);

        AssetFileData expectedInfo2Canged = new AssetFileData();
        expectedInfo2Canged.setChecksum(checksum2changed);
        expectedInfo2Canged.setFileName("asset2.txt");

        assertThat(as(SUPER_ADMIN).fetchAssetDetails(assetId).getFiles()).containsOnly(expectedInfo2Canged);

        output = TestFileReader.readTextFromFile(as(SUPER_ADMIN).downloadAssetFile(assetId, "asset2.txt"));
        assertThat(output).isEqualTo("I am text file \"asset2.txt\" - but from folder example-2");

        /* delete complete asset */
        as(SUPER_ADMIN).deleteAsset(assetId);

        assertThat(as(SUPER_ADMIN).fetchAllAssetIds()).doesNotContain(assetId);
        assertThatThrownBy(() -> as(SUPER_ADMIN).fetchAssetDetails(assetId)).isInstanceOf(NotFound.class);
    }

}
