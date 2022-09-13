// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * These constants represent keys available at PDS runtime. Those keys are NOT
 * sent from SecHub to PDS, but are injected as environment variables from PDS
 * for launcher script. The environment variables can be found at
 * {@link PDSLauncherScriptEnvironmentConstants} - this class represents the
 * same, but as spring boot value keys (e.g. to use them in wrapper
 * applications)
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSDefaultRuntimeKeyConstants {

    public static final String RT_KEY_PDS_JOB_USER_MESSAGES_FOLDER = "pds.job.user.messages.folder";

    public static final String RT_KEY_PDS_JOB_EVENTS_FOLDER = "pds.job.events.folder";

    public static final String RT_KEY_PDS_JOB_METADATA_FILE = "pds.job.metadata.file";

    public static final String RT_KEY_PDS_JOB_EXTRACTED_BINARIES_FOLDER = "pds.job.extracted.binaries.folder";

    /**
     * The location of the result file which will be returned to SecHub as the
     * product result
     */
    public static final String RT_KEY_PDS_JOB_RESULT_FILE = "pds.job.result.file";

    public static final String RT_KEY_PDS_JOB_BINARIES_TAR_FILE = "pds.job.binaries.tar.file";

    public static final String RT_KEY_PDS_JOB_EXTRACTED_SOURCES_FOLDER = "pds.job.extracted.sources.folder";

    public static final String RT_KEY_PDS_JOB_HAS_EXTRACTED_BINARIES = "pds.job.has.extracted.binaries";

    public static final String RT_KEY_PDS_JOB_HAS_EXTRACTED_SOURCES = "pds.job.has.extracted.sources";

    @Deprecated
    public static final String RT_KEY_PDS_JOB_SOURCECODE_UNZIPPED_FOLDER = "pds.job.sourcecode.unzipped.folder";

    public static final String RT_KEY_PDS_JOB_SOURCECODE_ZIP_FILE = "pds.job.sourcecode.zip.file";

    public static final String RT_KEY_PDS_JOB_UUID = "pds.job.uuid";

    public static final String RT_KEY_PDS_JOB_WORKSPACE_LOCATION = "pds.job.workspace.location";

    public static final String RT_KEY_SECHUB_JOB_UUID = "sechub.job.uuid";

}
