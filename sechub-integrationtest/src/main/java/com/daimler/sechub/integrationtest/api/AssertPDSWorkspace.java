// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

public class AssertPDSWorkspace {
    
    public AssertPDSWorkspace hasUploadedFile(UUID pdsJobUUID, String fileName) {
        
        File expectedUploadFolder = new File(TestAPI.PDS_WORKSPACE_FOLDER,pdsJobUUID.toString());
        File uploadFolder = new File(expectedUploadFolder,"upload");
        File uploadedFile = new File(uploadFolder,fileName);
        for (int i=0;i<10;i++) {
            if (uploadedFile.exists()) {
                return this;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        fail("File does not exist at: "+uploadedFile.getAbsolutePath());
        return this;
    }
    
}
