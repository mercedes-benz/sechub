package com.mercedesbenz.sechub.commons.pds;

/**
 * These constants represent keys available at PDS runtime. Those keys are NOT
 * sent from SecHub to PDS, but are from PDS only.
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSDefaultRuntimeKeyConstants {

    public static final String RT_KEY_PDS_USER_MESSAGES_FOLDER = "pds.job.user.messages.folder";

    public static final String RT_KEY_PDS_JOB_METADATA_FILE = "pds.job.metadata.file";

    public static final String RT_KEY_PDS_JOB_EXTRACTED_SOURCE_FOLDER = "pds.job.extracted.sources.folder";

    public static final String RT_KEY_PDS_JOB_EXTRACTED_BINARIES_FOLDER = "pds.job.extracted.binaries.folder";
}
