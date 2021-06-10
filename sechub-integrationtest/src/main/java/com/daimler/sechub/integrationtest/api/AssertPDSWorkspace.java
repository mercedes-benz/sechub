// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

public class AssertPDSWorkspace {
    
    /**
     * Does check if wanted file exists - does multiple retries (3 seconds)
     * @param pdsJobUUID
     * @param path
     * @param fileName
     * @return assert object
     */
    public AssertPDSWorkspace containsFile(UUID pdsJobUUID, String path, String fileName) {
        
        File file = resolveFile(pdsJobUUID, path, fileName);
        for (int i=0;i<10;i++) {
            if (file.exists()) {
                return this;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        fail("File does not exist at: "+file.getAbsolutePath());
        return this;
    }
    
    /**
     * Does check if wanted file no longer exists - does multiple retries (3 seconds)
     * @param pdsJobUUID
     * @param path
     * @param fileName
     * @return assert object
     */
    public AssertPDSWorkspace containsNOTFile(UUID pdsJobUUID, String path, String fileName) {
        
        File file = resolveFile(pdsJobUUID, path, fileName);
        for (int i=0;i<10;i++) {
            if (!file.exists()) {
                return this;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        fail("File does exist at: "+file.getAbsolutePath());
        return this;
    }

    private File resolveFile(UUID pdsJobUUID, String path, String fileName) {
        File expectedWorkspaceFolder = new File(TestAPI.PDS_WORKSPACE_FOLDER,pdsJobUUID.toString());
        File folder = new File(expectedWorkspaceFolder,path);
        File file = new File(folder,fileName);
        return file;
    }
    
}
