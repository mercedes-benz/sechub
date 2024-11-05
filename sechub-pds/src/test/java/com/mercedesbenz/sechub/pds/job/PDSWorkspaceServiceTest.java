// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.test.TestConstants.*;
import static java.io.File.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.storage.PDSStorageInfoCollector;
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

    @BeforeAll
    static void beforeAll() throws IOException {
        workspaceRootFolderPath = TestUtil.createTempDirectoryInBuildFolder("pds_ws_test").toAbsolutePath().toString();
    }

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();

        storageService = mock(PDSMultiStorageService.class);
        storage = mock(JobStorage.class);
        storageInfoCollector = mock(PDSStorageInfoCollector.class);
        preparationContextFactory = mock(PDSWorkspacePreparationContextFactory.class);
        serverConfigService = mock(PDSServerConfigurationService.class);
        preparationResultCalculator = mock(PDSWorkspacePreparationResultCalculator.class);

        preparationContext = mock(PDSWorkspacePreparationContext.class);
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
        assertSame(expected, result);
    }

    @Test
    @DisplayName("When job has no former meta data, there is no metadata file preparated")
    void when_job_has_no_metadata_no_metadata_file_is_created_in_workspace() throws Exception {
        /* execute */
        serviceToTest.prepare(jobUUID, config, null);

        /* test */
        File metaDataFile = serviceToTest.getMetaDataFile(jobUUID);
        assertFalse(metaDataFile.exists());

    }

    @Test
    @DisplayName("When job has former meta data, the workspace will have a filled metadata.txt is preparated")
    void when_job_has_metadata_a_metadata_file_is_created_in_workspace_containing_content() throws Exception {
        /* execute */
        serviceToTest.prepare(jobUUID, config, "this is my metadata");

        /* test */
        File metaDataFile = serviceToTest.getMetaDataFile(jobUUID);
        assertTrue(metaDataFile.exists());
        assertEquals("this is my metadata", TestFileReader.readTextFromFile(metaDataFile));
    }

    @Test
    @DisplayName("createLocationData method contains expected pathes when using temp directory for upload")
    void createLocationData_contains_expected_pathes_when_using_temp_directory_as_upload_base_path() throws Exception {
        /* execute */
        WorkspaceLocationData result = serviceToTest.createLocationData(jobUUID);

        /* test */
        String expectedWorspaceLocation = workspaceRootFolderPath + separatorChar + jobUUID;

        /* @formatter:off */
        assertEquals(expectedWorspaceLocation,result.getWorkspaceLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"result.txt",result.getResultFileLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"messages",result.getUserMessagesLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"metadata.txt",result.getMetaDataFileLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+SOURCECODE_ZIP,result.getSourceCodeZipFileLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"extracted"+separatorChar+"sources",result.getExtractedSourcesLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"extracted"+separatorChar+"binaries",result.getExtractedBinariesLocation());
        assertEquals(expectedWorspaceLocation+separatorChar+"events",result.getEventsLocation());
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

    private PDSExecutionParameterEntry createEntry(String key, String value) {
        PDSExecutionParameterEntry entry = new PDSExecutionParameterEntry();
        entry.setKey(key);
        entry.setValue(value);
        return entry;
    }

}
