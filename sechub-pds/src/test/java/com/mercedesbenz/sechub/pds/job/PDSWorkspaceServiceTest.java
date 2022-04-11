// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static java.io.File.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.storage.PDSStorageInfoCollector;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.test.TestConstants;
import com.mercedesbenz.sechub.test.TestUtil;

class PDSWorkspaceServiceTest {

    private PDSWorkspaceService serviceToTest;
    private PDSMultiStorageService storageService;
    private JobStorage storage;
    private PDSStorageInfoCollector storageInfoCollector;

    @BeforeEach
    void beforeEach() {

        storageService = mock(PDSMultiStorageService.class);
        storage = mock(JobStorage.class);
        storageInfoCollector = mock(PDSStorageInfoCollector.class);

        when(storageService.getJobStorage(any(), any())).thenReturn(storage);

        serviceToTest = new PDSWorkspaceService();
        serviceToTest.storageService = storageService;
        serviceToTest.storageInfoCollector = storageInfoCollector;
    }

    @Test
    @DisplayName("createLocationData method contains expected pathes when using temp directory for upload")
    void createLocationData_contains_expected_pathes_when_using_temp_directory_as_upload_base_path() throws Exception {
        /* prepare */
        String path = TestUtil.createTempDirectoryInBuildFolder("pds_ws_test").toAbsolutePath().toString();
        serviceToTest.uploadBasePath = path;

        UUID jobUUID = UUID.randomUUID();

        /* execute */
        WorkspaceLocationData result = serviceToTest.createLocationData(jobUUID);

        /* test */
        String expectedWorspaceLocation = path + separatorChar + "workspace" + separatorChar + jobUUID;

        /* @formatter:off */
        assertEquals(expectedWorspaceLocation,result.workspaceLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"result.txt",result.resultFileLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+TestConstants.SOURCECODE_ZIP,result.zippedSourceLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"unzipped"+separatorChar+"sourcecode",result.unzippedSourceLocation);
        /* @formatter:on */
    }

    @Test
    void when_configuration_tells_to_use_sechubstorage_sechub_storage_path_and_sechub_job_uuid_are_used() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        UUID secHubJobUUID = UUID.randomUUID();

        PDSJobConfiguration config = new PDSJobConfiguration();
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "true"));
        config.getParameters().add(createEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH, "xyz/abc/project1"));
        config.setSechubJobUUID(secHubJobUUID);

        when(storage.listNames()).thenReturn(Collections.singleton("something.zip"));
        when(storage.fetch("something.zip")).thenReturn(new ByteArrayInputStream("testme".getBytes()));

        /* execute */
        serviceToTest.prepareWorkspace(jobUUID, config);

        /* test */
        verify(storageService).getJobStorage("xyz/abc/project1", secHubJobUUID);
    }

    private PDSExecutionParameterEntry createEntry(String key, String value) {
        PDSExecutionParameterEntry entry = new PDSExecutionParameterEntry();
        entry.setKey(key);
        entry.setValue(value);
        return entry;
    }

}
