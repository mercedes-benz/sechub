// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.ConfigurationFailureException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData.PDSAssetData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetService;
import com.mercedesbenz.sechub.domain.scan.template.RelevantScanTemplateDefinitionFilter;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

class PDSTemplateMetaDataServiceTest {
    private PDSTemplateMetaDataService serviceToTest;
    private AssetService assetService;
    private RelevantScanTemplateDefinitionFilter filter;

    @BeforeEach
    void beforeEach() {

        filter = mock();
        assetService = mock();

        serviceToTest = new PDSTemplateMetaDataService();
        serviceToTest.filter = filter;
        serviceToTest.assetService = assetService;
    }

    @Test
    void createTemplateMetaData_returns_pds_template_meta_data_when_asset_is_available() throws Exception {

        /* prepare */
        ScanType exampleScanType = ScanType.WEB_SCAN;
        TemplateType exampleTemplateType = TemplateType.WEBSCAN_LOGIN;
        String exampleTemplateId1 = "template_id_1";
        String exampleChecksum1 = "checksum1";
        String exampleAssetId1 = "asset1";

        SecHubConfigurationModel configuration = mock();

        // prepare template data
        List<TemplateDefinition> givenDefinitions = mock();
        List<TemplateDefinition> filteredDefinitions = new ArrayList<>();
        TemplateVariable variable1 = new TemplateVariable();
        variable1.setName("var1");

        TemplateDefinition templateDefinition1 = new TemplateDefinition();
        templateDefinition1.setAssetId(exampleAssetId1);
        templateDefinition1.setId(exampleTemplateId1);
        templateDefinition1.setType(exampleTemplateType);

        templateDefinition1.getVariables().add(variable1);
        filteredDefinitions.add(templateDefinition1);

        when(filter.filter(givenDefinitions, exampleScanType, configuration)).thenReturn(filteredDefinitions);

        // prepare asset data
        AssetFileData assetFile1a = new AssetFileData();
        assetFile1a.setChecksum("other");
        assetFile1a.setFileName("other_product_id.zip");
        AssetDetailData assetDetailData1 = mock();

        AssetFileData assetFile1b = new AssetFileData();
        assetFile1b.setChecksum(exampleChecksum1);
        assetFile1b.setFileName("test_product_id.zip");

        List<AssetFileData> assetfiles = new ArrayList<>();
        assetfiles.add(assetFile1a);
        assetfiles.add(assetFile1b);

        when(assetDetailData1.getFiles()).thenReturn(assetfiles);
        when(assetService.fetchAssetDetails(exampleAssetId1)).thenReturn(assetDetailData1);

        /* execute */
        List<PDSTemplateMetaData> result = serviceToTest.createTemplateMetaData(givenDefinitions, "test_product_id", exampleScanType, configuration);

        /* test */
        PDSTemplateMetaData expectedTemplateMetaData1 = new PDSTemplateMetaData();
        expectedTemplateMetaData1.setTemplateId(exampleTemplateId1);
        expectedTemplateMetaData1.setTemplateType(exampleTemplateType);

        PDSAssetData assetData1 = new PDSAssetData();
        assetData1.setAssetId(exampleAssetId1);
        assetData1.setChecksum(exampleChecksum1);
        assetData1.setFileName("test_product_id.zip");

        expectedTemplateMetaData1.setAssetData(assetData1);

        assertThat(result).isNotNull().contains(expectedTemplateMetaData1).hasSize(1);
    }

    @Test
    void createTemplateMetaData_returns_empty_pds_template_meta_data_when_definition_list_is_empty() throws Exception {

        /* prepare */
        ScanType exampleScanType = ScanType.WEB_SCAN;

        SecHubConfigurationModel configuration = mock();

        // prepare template data
        List<TemplateDefinition> givenDefinitions = mock();
        List<TemplateDefinition> filteredDefinitions = new ArrayList<>();

        when(filter.filter(givenDefinitions, exampleScanType, configuration)).thenReturn(filteredDefinitions);

        /* execute */
        List<PDSTemplateMetaData> result = serviceToTest.createTemplateMetaData(givenDefinitions, "test_product_id", exampleScanType, configuration);

        /* test */
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void createTemplateMetaData_throws_exception_when_asset_not_found() throws Exception {

        /* prepare */
        ScanType exampleScanType = ScanType.WEB_SCAN;
        TemplateType exampleTemplateType = TemplateType.WEBSCAN_LOGIN;
        String exampleTemplateId1 = "template_id_1";
        String exampleAssetId1 = "asset1";

        SecHubConfigurationModel configuration = mock();

        // prepare template data
        List<TemplateDefinition> givenDefinitions = mock();
        List<TemplateDefinition> filteredDefinitions = new ArrayList<>();
        TemplateVariable variable1 = new TemplateVariable();
        variable1.setName("var1");

        TemplateDefinition templateDefinition1 = new TemplateDefinition();
        templateDefinition1.setAssetId(exampleAssetId1);
        templateDefinition1.setId(exampleTemplateId1);
        templateDefinition1.setType(exampleTemplateType);

        templateDefinition1.getVariables().add(variable1);
        filteredDefinitions.add(templateDefinition1);

        when(filter.filter(givenDefinitions, exampleScanType, configuration)).thenReturn(filteredDefinitions);

        when(assetService.fetchAssetDetails(exampleAssetId1)).thenThrow(NotFoundException.class);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.createTemplateMetaData(givenDefinitions, "test_product_id", exampleScanType, configuration))
                .isInstanceOf(ConfigurationFailureException.class).hasMessageContaining(exampleAssetId1 + " does not exist");
    }

    @Test
    void createTemplateMetaData_throws_exception_when_asset_product_file_not_found() throws Exception {

        /* prepare */
        ScanType exampleScanType = ScanType.WEB_SCAN;
        TemplateType exampleTemplateType = TemplateType.WEBSCAN_LOGIN;
        String exampleTemplateId1 = "template_id_1";
        String exampleAssetId1 = "asset1";

        SecHubConfigurationModel configuration = mock();

        // prepare template data
        List<TemplateDefinition> givenDefinitions = mock();
        List<TemplateDefinition> filteredDefinitions = new ArrayList<>();
        TemplateVariable variable1 = new TemplateVariable();
        variable1.setName("var1");

        TemplateDefinition templateDefinition1 = new TemplateDefinition();
        templateDefinition1.setAssetId(exampleAssetId1);
        templateDefinition1.setId(exampleTemplateId1);
        templateDefinition1.setType(exampleTemplateType);

        templateDefinition1.getVariables().add(variable1);
        filteredDefinitions.add(templateDefinition1);

        when(filter.filter(givenDefinitions, exampleScanType, configuration)).thenReturn(filteredDefinitions);
        // prepare asset data
        AssetFileData assetFile1a = new AssetFileData();
        assetFile1a.setChecksum("other");
        assetFile1a.setFileName("other_product_id.zip");
        AssetDetailData assetDetailData1 = mock();

        List<AssetFileData> assetfiles = new ArrayList<>();
        assetfiles.add(assetFile1a);

        when(assetDetailData1.getFiles()).thenReturn(assetfiles);
        when(assetService.fetchAssetDetails(exampleAssetId1)).thenReturn(assetDetailData1);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.createTemplateMetaData(givenDefinitions, "test_product_id", exampleScanType, configuration))
                .isInstanceOf(ConfigurationFailureException.class).hasMessageContaining("does not contain file 'test_product_id.zip'");
        ;
    }

    @Test
    void ensureTemplateAssetFilesAreAvailableInStorage_calls_asste_service_for_each_metadata_assetfile() throws Exception {

        /* prepare */
        PDSAssetData assetData1 = new PDSAssetData();
        assetData1.setAssetId("asset1");
        assetData1.setFileName("file1.txt");

        PDSTemplateMetaData templateMetaData1 = new PDSTemplateMetaData();
        templateMetaData1.setAssetData(assetData1);

        PDSAssetData assetData2 = new PDSAssetData();
        assetData2.setAssetId("asset2");
        assetData2.setFileName("file2.txt");

        PDSTemplateMetaData templateMetaData2 = new PDSTemplateMetaData();
        templateMetaData2.setAssetData(assetData2);

        List<PDSTemplateMetaData> metaDataList = new ArrayList<>();
        metaDataList.add(templateMetaData1);
        metaDataList.add(templateMetaData2);

        /* execute */
        serviceToTest.ensureTemplateAssetFilesAreAvailableInStorage(metaDataList);

        /* test */
        verify(assetService).ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase("file1.txt", "asset1");
        verify(assetService).ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase("file2.txt", "asset2");
    }

}
