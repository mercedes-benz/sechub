package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import java.util.Map;

import javax.crypto.SealedObject;

public class GitContext {
    String location;
    boolean cloneWithoutHistory;
    Map<String, SealedObject> credentialMap;
    String pdsPrepareUploadFolderDirectory;

    public GitContext(String location, boolean cloneWithoutHistory, Map<String, SealedObject> credentialMap, String pdsPrepareUploadFolderDirectory) {
        this.location = location;
        this.cloneWithoutHistory = cloneWithoutHistory;
        this.credentialMap = credentialMap;
        this.pdsPrepareUploadFolderDirectory = pdsPrepareUploadFolderDirectory;
    }
}
