// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

/**
 * These constants are used for environment variables given from PDS to launcher
 * scripts
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSLauncherScriptEnvironmentConstants {

    /*
     * TODO Albert Tregnaghi, 2022-05-22: This is deprecated becausee we will remove
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
    public static final String PDS_JOB_RESULT_FILE = "PDS_JOB_RESULT_FILE";
}
