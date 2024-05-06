// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * These constants are PDS internal and do not come from SecHub job or executor
 * configurations. The job parameter parts from outside (e.g. SecHub) are also
 * available inside the launcher scripts, but only when automatically accepted
 * (see {@link PDSConfigDataKeyProvider} in combination with
 * `markAsAvailableInsideScript` ) or configured manually inside the PDS server
 * configuration file. <br>
 * <br>
 * There exists a spring boot key pendant :
 * {@link PDSDefaultRuntimeKeyConstants}
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSLauncherScriptEnvironmentConstants {

    public static final String SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
    /*
     * TODO Albert Tregnaghi, 2022-05-22: This is deprecated because we will remove
     * the old environment variable PDS_JOB_SOURCECODE_UNZIPPED_FOLDER when all
     * existing PDS solutions have been migrated to new environment variable
     * PDS_JOB_EXTRACTED_SOURCES_FOLDER
     */
    @Deprecated
    public static final String PDS_JOB_SOURCECODE_UNZIPPED_FOLDER = "PDS_JOB_SOURCECODE_UNZIPPED_FOLDER";

    public static final String PDS_JOB_EXTRACTED_SOURCES_FOLDER = "PDS_JOB_EXTRACTED_SOURCES_FOLDER";
    public static final String PDS_JOB_HAS_EXTRACTED_SOURCES = "PDS_JOB_HAS_EXTRACTED_SOURCES";
    public static final String PDS_JOB_SOURCECODE_ZIP_FILE = "PDS_JOB_SOURCECODE_ZIP_FILE";

    public static final String PDS_JOB_EXTRACTED_BINARIES_FOLDER = "PDS_JOB_EXTRACTED_BINARIES_FOLDER";
    public static final String PDS_JOB_HAS_EXTRACTED_BINARIES = "PDS_JOB_HAS_EXTRACTED_BINARIES";
    public static final String PDS_JOB_BINARIES_TAR_FILE = "PDS_JOB_BINARIES_TAR_FILE";

    public static final String PDS_JOB_WORKSPACE_LOCATION = "PDS_JOB_WORKSPACE_LOCATION";

    /**
     * The location of the result file which will be returned to SecHub as the
     * product result
     */
    public static final String PDS_JOB_RESULT_FILE = "PDS_JOB_RESULT_FILE";

    public static final String PDS_JOB_UUID = "PDS_JOB_UUID";

    public static final String PDS_JOB_USER_MESSAGES_FOLDER = "PDS_JOB_USER_MESSAGES_FOLDER";

    public static final String PDS_JOB_EVENTS_FOLDER = "PDS_JOB_EVENTS_FOLDER";

    public static final String PDS_JOB_METADATA_FILE = "PDS_JOB_METADATA_FILE";
}
