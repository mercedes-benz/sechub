// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.test.TestConstants.*;
import static java.io.File.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData.PDSAssetData;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.storage.PDSStorageInfoCollector;
import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestUtil;

class PDSWorkspaceServiceTest {

    private PDSWorkspaceService serviceToTest;
    private PDSMultiStorageService storageService;
    private JobStorage storage;
    private PDSStorageInfoCollector storageInfoCollector;
    private PDSJobConfiguration config;
    private static String workspaceRootFolderPath;
    private PDSWorkspacePreparationContextFactory preparationContextFactory;
    private PDSServerConfigurationService serverConfigService;
    private PDSWorkspacePreparationResultCalculator preparationResultCalculator;
    private PDSWorkspacePreparationContext preparationContext;
    private UUID jobUUID;
    private CheckSumSupport checksumSupport;

    @BeforeAll
    static void beforeAll() throws IOException {
        workspaceRootFolderPath = TestUtil.createTempDirectoryInBuildFolder("pds_ws_test").toAbsolutePath().toString();
    }

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();

        storageService = mock();
        storage = mock();
        storageInfoCollector = mock();
        preparationContextFactory = mock();
        serverConfigService = mock();
        preparationResultCalculator = mock();
        checksumSupport = mock();

        preparationContext = mock();
        when(preparationContextFactory.createPreparationContext(any())).thenReturn(preparationContext);

        PDSProductSetup setup = new PDSProductSetup();
        when(serverConfigService.getProductSetupOrNull(any())).thenReturn(setup);

        when(storageService.createJobStorageForPath(any(), any())).thenReturn(storage);

        serviceToTest = new PDSWorkspaceService();
        serviceToTest.storageService = storageService;
        serviceToTest.storageInfoCollector = storageInfoCollector;
        serviceToTest.workspaceRootFolderPath = workspaceRootFolderPath;
        serviceToTest.preparationContextFactory = preparationContextFactory;
        serviceToTest.serverConfigService = serverConfigService;
        serviceToTest.preparationResultCalculator = preparationResultCalculator;
        serviceToTest.checksumSupport = checksumSupport;

        config = new PDSJobConfiguration();

        UUID secHubJobUUID = UUID.randomUUID();
        config.setSechubJobUUID(secHubJobUUID);

    }

    @Test
    void prepare_uses_prepare_factory() throws Exception {
        /* execute */
        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        verify(preparationContextFactory).createPreparationContext(any());
    }

    @Test
    void prepare_does_not_set_extracted_sources_when_no_source_accepted() throws Exception {
        /* execute */
        when(preparationContext.isSourceAccepted()).thenReturn(false);

        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        verify(preparationContext, never()).setExtractedSourceAvailable(any(Boolean.class));
    }

    @Test
    void prepare_does_set_extracted_sources_when_source_accepted() throws Exception {
        /* execute */
        when(preparationContext.isSourceAccepted()).thenReturn(true);

        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        verify(preparationContext).setExtractedSourceAvailable(any(Boolean.class));
    }

    @Test
    void prepare_does_not_set_extracted_binaries_when_no_binaries_accepted() throws Exception {
        /* execute */
        when(preparationContext.isBinaryAccepted()).thenReturn(false);

        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        verify(preparationContext, never()).setExtractedBinaryAvailable(any(Boolean.class));
    }

    @Test
    void prepare_does_set_extracted_binaries_when_no_binaries_accepted() throws Exception {
        /* execute */
        when(preparationContext.isBinaryAccepted()).thenReturn(true);

        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        verify(preparationContext).setExtractedBinaryAvailable(any(Boolean.class));
    }

    @Test
    void prepare_returns_result_from_calculator() throws Exception {
        /* prepare */
        PDSWorkspacePreparationResult expected = new PDSWorkspacePreparationResult(true);

        when(preparationResultCalculator.calculateResult(preparationContext)).thenReturn(expected);

        /* execute */
        PDSWorkspacePreparationResult result = serviceToTest.prepare(jobUUID, config, null);

        /* test */
        assertThat(expected).isSameAs(result);
    }

    @Test
    @DisplayName("When job has no former meta data, there is no metadata file preparated")
    void when_job_has_no_metadata_no_metadata_file_is_created_in_workspace() throws Exception {
        /* execute */
        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        File metaDataFile = serviceToTest.getMetaDataFile(jobUUID);
        assertThat(metaDataFile.exists()).isFalse();

    }

    @Test
    @DisplayName("When job has former meta data, the workspace will have a filled metadata.txt is preparated")
    void when_job_has_metadata_a_metadata_file_is_created_in_workspace_containing_content() throws Exception {
        /* execute */
        serviceToTest.prepare(jobUUID, config, "this is my metadata");

        /* test */
        File metaDataFile = serviceToTest.getMetaDataFile(jobUUID);
        assertThat(metaDataFile.exists()).isTrue();
        assertThat(TestFileReader.readTextFromFile(metaDataFile)).isEqualTo("this is my metadata");
    }

    @Test
    @DisplayName("createLocationData method contains expected pathes when using temp directory for upload")
    void createLocationData_contains_expected_pathes_when_using_temp_directory_as_upload_base_path() throws Exception {
        /* execute */
        WorkspaceLocationData result = serviceToTest.createLocationData(jobUUID);

        /* test */
        String expectedWorspaceLocation = workspaceRootFolderPath + separatorChar + jobUUID;

        /* @formatter:off */
        assertThat(result.getWorkspaceLocation()).isEqualTo(expectedWorspaceLocation);
        assertThat(result.getResultFileLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"result.txt");

        assertThat(result.getUserMessagesLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"messages");
        assertThat(result.getMetaDataFileLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"metadata.txt");
        assertThat(result.getSourceCodeZipFileLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+SOURCECODE_ZIP);
        assertThat(result.getExtractedSourcesLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"extracted"+separatorChar+"sources");
        assertThat(result.getExtractedBinariesLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"extracted"+separatorChar+"binaries");
        assertThat(result.getEventsLocation()).isEqualTo(expectedWorspaceLocation+separatorChar+"events");
        /* @formatter:on */
    }

    @Test
    void when_configuration_tells_to_use_sechubstorage_sechub_storage_path_and_sechub_job_uuid_are_used() throws Exception {
        /* prepare */
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "true"));
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH, "xyz/abc/project1"));

        when(storage.listNames()).thenReturn(Collections.singleton("something.zip"));
        when(storage.fetch("something.zip")).thenReturn(new ByteArrayInputStream("testme".getBytes()));

        /* execute */
        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        verify(storageService).createJobStorageForPath("xyz/abc/project1", config.getSechubJobUUID());
    }

    @Test
    void prepare_downloads_asset_and_stores_file_locally_when_parameter_contains_pds_template_metadata_no_checksum_failure() throws Exception {
        /* prepare */
        PDSTemplateMetaData metaData = new PDSTemplateMetaData();
        metaData.setTemplateId("template1");
        metaData.setTemplateType(TemplateType.WEBSCAN_LOGIN);
        PDSAssetData assetData = new PDSAssetData();
        assetData.setAssetId("asset1");
        assetData.setChecksum("checksum1");
        assetData.setFileName("file1.txt");
        metaData.setAssetData(assetData);

        String json = JSONConverter.get().toJSON(metaData, false);
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_TEMPLATE_META_DATA_LIST, json));

        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage("asset1")).thenReturn(assetStorage);
        when(assetStorage.fetch("file1.txt")).thenReturn(new ByteArrayInputStream("testdata".getBytes()));
        when(checksumSupport.createSha256Checksum(any(Path.class))).thenReturn("checksum1");

        /* execute */
        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.captor();
        verify(storageService).createAssetStorage("asset1");
        verify(checksumSupport).createSha256Checksum(pathCaptor.capture());
        Path path = pathCaptor.getValue();

        assertThat(path.getFileName().toString()).isEqualTo("file1.txt");

        // check file is created
        assertThat(Files.exists(path)).isTrue();
        List<String> lines = Files.readAllLines(path);
        assertThat(lines).contains("testdata").hasSize(1);
    }

    @Test
    void prepare_downloads_asset_and_stores_file_locally_when_parameter_contains_pds_template_metadata_checksum_failure() throws Exception {
        /* prepare */
        PDSTemplateMetaData metaData = new PDSTemplateMetaData();
        metaData.setTemplateId("template1");
        metaData.setTemplateType(TemplateType.WEBSCAN_LOGIN);
        PDSAssetData assetData = new PDSAssetData();
        assetData.setAssetId("asset1");
        assetData.setChecksum("checksum1");
        assetData.setFileName("file1.txt");
        metaData.setAssetData(assetData);

        String json = JSONConverter.get().toJSON(metaData, false);
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_TEMPLATE_META_DATA_LIST, json));

        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage("asset1")).thenReturn(assetStorage);
        when(assetStorage.fetch("file1.txt")).thenReturn(new ByteArrayInputStream("testdata".getBytes()));
        when(checksumSupport.createSha256Checksum(any(Path.class))).thenReturn("checksum-other-means-failure");

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.prepare(jobUUID, config, null)).cause().isInstanceOf(IOException.class)
                .hasMessageStartingWith("Checksum not as expected");

    }

    private PDSExecutionParameterEntry createEntry(String key, String value) {
        PDSExecutionParameterEntry entry = new PDSExecutionParameterEntry();
        entry.setKey(key);
        entry.setValue(value);
        return entry;
    }

}
