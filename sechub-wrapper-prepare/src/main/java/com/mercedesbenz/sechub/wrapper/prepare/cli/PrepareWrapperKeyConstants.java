package com.mercedesbenz.sechub.wrapper.prepare.cli;

public class PrepareWrapperKeyConstants {

    /**
     * Folder for PDS prepare where remote data gets downloaded to be uploaded to
     * the shared storage
     */
    public static final String KEY_PDS_PREPARE_UPLOAD_FOLDER_DIRECTORY = "pds.prepare.upload.folder.directory";

    /**
     * Flag to enable different prepare modules
     */
    public static final String KEY_PDS_PREPARE_MODULE_ENABLED_GIT = "pds.prepare.module.enabled.git";

    /**
     * GIT specific variables Flag to enable auto cleanup of the git folder after
     * the prepare process, if set to false, git folder will not be deleted and
     * cloned with full history
     */
    public static final String KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER = "pds.prepare.auto.cleanup.git.folder";

    public static final String KEY_PDS_PREPARE_MINUTES_TO_WAIT_PREPARE_PROCESSES = "pds.prepare.wait.for.download.remote.data";

}
