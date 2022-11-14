// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * All default parameter keys supported by PDS. A PDS can support optional
 * parameters (via its configuration) but these ones are always supported and be
 * available at runtime inside PDS scripts.<br>
 * <br>
 *
 * Wrappers can use these constants as spring boot values.
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSDefaultParameterKeyConstants {

    /* ------------------------------------------------------- */
    /* SecHub execution information ("dynamic" - job dependent */
    /* ------------------------------------------------------- */
    public static final String PARAM_KEY_SECHUB_JOB_UUID = "sechub.job.uuid";

    public static final String PARAM_KEY_PDS_SCAN_TARGET_TYPE = "pds.scan.target.type";

    public static final String PARAM_KEY_PDS_SCAN_TARGET_URL = "pds.scan.target.url";

    public static final String PARAM_KEY_PDS_DEBUG_ENABLED = "pds.debug.enabled";

    /**
     * Contains the SecHub configuration model as Json
     */
    public static final String PARAM_KEY_PDS_SCAN_CONFIGURATION = "pds.scan.configuration";

    /* ----------------------------------------------- */
    /* SecHub execution configuration parts ("static") */
    /* ----------------------------------------------- */
    public static final String PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER = "pds.config.productidentifier";

    public static final String PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE = "pds.config.use.sechub.storage";

    public static final String PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS = "pds.config.use.sechub.mappings";

    public static final String PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH = "pds.config.sechub.storage.path";

    public static final String PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES = "pds.config.filefilter.excludes";

    public static final String PARAM_KEY_PDS_CONFIG_FILEFILTER_INCLUDES = "pds.config.filefilter.includes";

    public static final String PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED = "pds.config.script.trustall.certificates.enabled";

    /**
     * Maximum time the launcher script process will be kept alive when the PDS job
     * has been canceled.
     */
    public static final String PARAM_KEY_PDS_CONFIG_CANCEL_MAXIMUM_WAITTIME_SECONDS = "pds.config.cancel.maximum.waittime.seconds";

    /**
     * The time in milliseconds PDS will check again if the launcher script process
     * is alive or not when the PDS job has been canceled.
     */
    public static final String PARAM_KEY_PDS_CONFIG_CANCEL_EVENT_CHECKINTERVAL_MILLISECONDS = "pds.config.cancel.event.checkinterval.milliseconds";

    /* ---------------------- */
    /* Integration tests only */
    /* ---------------------- */
    public static final String PARAM_KEY_PDS_MOCKING_DISABLED = "pds.mocking.disabled";

}
