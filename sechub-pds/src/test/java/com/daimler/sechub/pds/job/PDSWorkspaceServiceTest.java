// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static java.io.File.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PDSWorkspaceServiceTest {
    
    private PDSWorkspaceService serviceToTest;

    @BeforeEach
    void beforeEach() {
        serviceToTest=new PDSWorkspaceService();
    }

    @Test
    @DisplayName("createLocationData method contains expected pathes when using temp directory for upload")
    void createLocationData_contains_expected_pathes_when_using_temp_directory_as_upload_base_path() throws Exception{
        /* prepare */
        String path = Files.createTempDirectory("pds_ws_test").toAbsolutePath().toString();
        serviceToTest.uploadBasePath=path;
        
        UUID jobUUID = UUID.randomUUID();
        
        /* execute */
        WorkspaceLocationData result = serviceToTest.createLocationData(jobUUID);
        
        /* test */
        String expectedWorspaceLocation = path+separatorChar+"workspace"+separatorChar+jobUUID;
        
        assertEquals(expectedWorspaceLocation,result.workspaceLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"output"+separatorChar+"result.txt",result.resultFileLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"sourcecode.zip",result.zippedSourceLocation);
        assertEquals(expectedWorspaceLocation+separatorChar+"upload"+separatorChar+"unzipped"+separatorChar+"sourcecode",result.unzippedSourceLocation);
    }

}
