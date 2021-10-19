// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.pds;

/**
 * All default parameter keys supported by PDS. A PDS can support optional
 * parameters (via its configuration) but these ones are always supported and be
 * available at runtime inside PDS scripts.
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSDefaultParameterKeyConstants {

    public static final String PARAM_KEY_TARGET_TYPE = "pds.scan.target.type";

    public static final String PARAM_KEY_TARGET_URL = "pds.scan.target.url";

    public static final String PARAM_KEY_PRODUCT_IDENTIFIER = "pds.config.productidentifier";

    public static final String PARAM_KEY_USE_SECHUB_STORAGE = "pds.config.use.sechub.storage";

    public static final String PARAM_KEY_SECHUB_STORAGE_PATH = "pds.config.sechub.storage.path";

}
