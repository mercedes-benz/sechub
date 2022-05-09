// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * All default parameter keys supported by PDS. A PDS can support optional
 * parameters (via its configuration) but these ones are always supported and be
 * available at runtime inside PDS scripts.
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSDefaultParameterKeyConstants {

    /* ----------------------------------------------- */
    /* SecHub execution information ("dynamic" - job dependent */
    /* ----------------------------------------------- */
    public static final String PARAM_KEY_PDS_SCAN_TARGET_TYPE = "pds.scan.target.type";

    public static final String PARAM_KEY_PDS_SCAN_TARGET_URL = "pds.scan.target.url";

    /**
     * Contains the SecHub configuration model as Json
     */
    public static final String PARAM_KEY_PDS_SCAN_CONFIGURATION = "pds.scan.configuration";

    /* ----------------------------------------------- */
    /* SecHub execution configuration parts ("static") */
    /* ----------------------------------------------- */
    public static final String PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER = "pds.config.productidentifier";

    public static final String PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE = "pds.config.use.sechub.storage";

    public static final String PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH = "pds.config.sechub.storage.path";

}
