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

    @BeforeAll
    static void beforeAll() throws IOException {
        workspaceRootFolderPath = TestUtil.createTempDirectoryInBuildFolder("pds_ws_test").toAbsolutePath().toString();
    }

    @BeforeEach
    void beforeEach() {

        storageService = mock(PDSMultiStorageService.class);
        storage = mock(JobStorage.class);
        storageInfoCollector = mock(PDSStorageInfoCollector.class);

        when(storageService.getJobStorage(any(), any())).thenReturn(storage);

        serviceToTest = new PDSWorkspaceService();
        serviceToTest.storageService = storageService;
        serviceToTest.storageInfoCollector = storageInfoCollector;
        serviceToTest.workspaceRootFolderPath = workspaceRootFolderPath;

        config = new PDSJobConfiguration();

        UUID secHubJobUUID = UUID.randomUUID();
        config.setSechubJobUUID(secHubJobUUID);

    }

    @Test
    @DisplayName("When job has no former meta data, there is no metadata file preparated")
    void when_job_has_no_metadata_no_metadata_file_is_created_in_workspace() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        /* execute */
        serviceToTest.prepareWorkspace(jobUUID, config, null);

        /* test */
        File metaDataFile = serviceToTest.getMetaDataFile(jobUUID);
        assertFalse(metaDataFile.exists());

    }

    @Test
    @DisplayName("When job has former meta data, the workspace will have a filled metadata.txt is preparated")
    void when_job_has_metadata_a_metadata_file_is_created_in_workspace_containing_content() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        /* execute */
        serviceToTest.prepareWorkspace(jobUUID, config, "this is my metadata");

        /* test */
        File metaDataFile = serviceToTest.getMetaDataFile(jobUUID);
        assertTrue(metaDataFile.exists());
        assertEquals("this is my metadata", TestFileReader.loadTextFile(metaDataFile));
    }

    @Test
    @DisplayName("createLocationData method contains expected pathes when using temp directory for upload")
    void createLocationData_contains_expected_pathes_when_using_temp_directory_as_upload_base_path() throws Exception {
        /* prepare */

        UUID jobUUID = UUID.randomUUID();

        /* execute */
        WorkspaceLocationData result = serviceToTest.createLocationData(jobUUID);

        /* test */
        String expectedWorspaceLocation = workspaceRootFolderPath + separatorChar + "workspace" + separatorChar + jobUUID;

        /* @formatter:off */
        assertEquals(expectedWorspaceLocation,result.workspaceLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"result.txt",result.resultFileLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"messages",result.userMessagesLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"metadata.txt",result.metaDataFileLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+SOURCECODE_ZIP,result.sourceCodeZipFileLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"extracted"+separatorChar+"sources",result.extractedSourcesLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"extracted"+separatorChar+"binaries",result.extractedBinariesLocation);
        /* @formatter:on */
    }

    @Test
    void when_configuration_tells_to_use_sechubstorage_sechub_storage_path_and_sechub_job_uuid_are_used() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "true"));
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH, "xyz/abc/project1"));

        when(storage.listNames()).thenReturn(Collections.singleton("something.zip"));
        when(storage.fetch("something.zip")).thenReturn(new ByteArrayInputStream("testme".getBytes()));

        /* execute */
        serviceToTest.prepareWorkspace(jobUUID, config, null);

        /* test */
        verify(storageService).getJobStorage("xyz/abc/project1", config.getSechubJobUUID());
    }

    private PDSExecutionParameterEntry createEntry(String key, String value) {
        PDSExecutionParameterEntry entry = new PDSExecutionParameterEntry();
        entry.setKey(key);
        entry.setValue(value);
        return entry;
    }

}
