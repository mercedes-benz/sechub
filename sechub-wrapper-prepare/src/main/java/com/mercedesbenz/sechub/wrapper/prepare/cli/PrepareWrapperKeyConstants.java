package com.mercedesbenz.sechub.wrapper.prepare.cli;

public class PrepareWrapperKeyConstants {

    /**
     * Folder for PDS prepare where remote data gets downloaded to be uploaded to
     * the shared storage
     */
    public static final String KEY_PDS_PREPARE_UPLOAD_FOLDER_DIRECTORY = "pds.prepare.upload.folder.directory";

    public static final String KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER = "pds.prepare.auto.cleanup.git.folder";

    /***
     * Credentials for remote Data download are and used within the application
     */
    public static final String KEY_PDS_PREPARE_CREDENTIAL_USERNAME = "pds.prepare.credential.username";

    public static final String KEY_PDS_PREPARE_CREDENTIAL_PASSWORD = "pds.prepare.credential.password";

    /**
     * Flag to enable the modules
     */
    public static final String KEY_PDS_PREPARE_MODULE_ENABLED_GIT = "pds.prepare.module.enabled.git";

}
