package com.mercedesbenz.sechub.wrapper.prepare.cli;

public class PrepareWrapperKeyConstants {

    /**
     * Folder for PDS prepare where remote data gets downloaded to be uploaded to
     * the shared storage
     */
    public static final String KEY_PDS_PREPARE_UPLOAD_DIRECTORY = "pds.prepare.upload.directory";

    /**
     * Prepare process timeout in seconds for prepare processes started with process
     * builder, default is -1 prepare process timeout can not be higher than the pds
     * product timeout
     */
    public static final String KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS = "pds.prepare.process.timeout.seconds";

    /**
     * Flag to enable the git prepare module
     */
    public static final String KEY_PDS_PREPARE_MODULE_GIT_ENABLED = "pds.prepare.module.git.enabled";

    /**
     * Flag to clean the git folder from git files and clone without history
     */
    public static final String KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER = "pds.prepare.auto.cleanup.git.folder";
}
