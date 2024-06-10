// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.cli;

public class PrepareWrapperKeyConstants {

    private static final String PREPARE_MODULE = "pds.prepare.module";
    private static final String PREPARE_MODULE_GIT = PREPARE_MODULE + ".git";
    private static final String PREPARE_MODULE_SKOPEO = PREPARE_MODULE + ".skopeo";

    /**
     * Prepare process timeout in seconds for prepare processes started with process
     * builder, default is -1 prepare process timeout can not be higher than the pds
     * product timeout
     */
    public static final String KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS = "pds.prepare.process.timeout.seconds";

    /**
     * Flag to enable the skopeoWrapper prepare module
     */
    public static final String KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED = PREPARE_MODULE_SKOPEO + ".enabled";

    /**
     * Filename for skopeoWrapper authentication file
     */
    public static final String KEY_PDS_PREPARE_MODULE_SKOPEO_AUTHENTICATION_FILENAME = PREPARE_MODULE_SKOPEO + ".authentication.filename";

    /**
     * Flag to enable the gitWrapper prepare module
     */
    public static final String KEY_PDS_PREPARE_MODULE_GIT_ENABLED = PREPARE_MODULE_GIT + ".enabled";

    public static final String KEY_PDS_PREPARE_MODULE_GIT_REMOVE_GIT_FILES_BEFORE_UPLOAD = PREPARE_MODULE_GIT + ".remove.gitfiles.before.upload";

    public static final String KEY_PDS_PREPARE_MODULE_GIT_CLONE_WITHOUT_GIT_HISTORY = PREPARE_MODULE_GIT + ".clone.without.git.history";

}
