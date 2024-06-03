// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.cli;

public class PrepareWrapperKeyConstants {

    /**
     * Prepare process timeout in seconds for prepare processes started with process
     * builder, default is -1 prepare process timeout can not be higher than the pds
     * product timeout
     */
    public static final String KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS = "pds.prepare.process.timeout.seconds";

    /**
     * Flag to enable the gitWrapper prepare module
     */
    public static final String KEY_PDS_PREPARE_MODULE_GIT_ENABLED = "pds.prepare.module.git.enabled";

    /**
     * Flag to enable the skopeoWrapper prepare module
     */
    public static final String KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED = "pds.prepare.module.skopeo.enabled";

    /**
     * Flag to clean the gitWrapper folder from git files and clone without history
     */
    public static final String KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER = "pds.prepare.auto.cleanup.git.folder";

    /**
     * Filename for skopeoWrapper authentication file
     */
    public static final String KEY_PDS_PREPARE_AUTHENTICATION_FILE_SKOPEO = "pds.prepare.authentication.file.skopeo";
}
