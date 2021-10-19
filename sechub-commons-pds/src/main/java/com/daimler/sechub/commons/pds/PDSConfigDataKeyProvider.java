// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.pds;

/**
 * Provides keys which are interpreted <strong>at PDS side</strong>
 * 
 * @author Albert Tregnaghi
 *
 */
public enum PDSConfigDataKeyProvider implements PDSKeyProvider<RuntimeEnvironmentKey> {

    /**
     * Special key inside executor configuration which will be used to define the
     * PDS product identifier! So this key is not inside job parameters, but will be
     * available as "productID" for PDS
     */
    PDS_PRODUCT_IDENTIFIER(new RuntimeEnvironmentKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PRODUCT_IDENTIFIER,
            "Contains the product identifier, so PDS knows which part is to call on it's side.").markMandatory()),

    /**
     * This is automatically given to PDS by SecHub on every call by adapter
     */
    PDS_TARGET_TYPE(new RuntimeEnvironmentKey(PDSDefaultParameterKeyConstants.PARAM_KEY_TARGET_TYPE,
            "Contains the target type (depending on scan type) and will be just an additional information for PDS from SecHub.").markGenerated()),

    /**
     * Special key inside executor configuration which will be used to define if PDS
     * will reuse SecHub storage locations. When storage location is reused the PDS
     * executor will not upload job data to PDS again and PDS server will reuse the
     * SecHub storage directly.
     */
    PDS_USE_SECHUB_STORAGE(new RuntimeEnvironmentKey(PDSDefaultParameterKeyConstants.PARAM_KEY_USE_SECHUB_STORAGE,
            "When 'true' the SecHub storage will be reused by PDS server. In this case SecHub will not upload job data to PDS.\n"
                    + "But it's crucial to have same root storage setup on PDS server side (e.g. same s3 bucket for S3 storage, or same NFS base for shared volumes).\n"
                    + "When not `true` or not defined, pds will use its own storage locations").markAlwaysSentToPDS().markDefaultRecommended()
                            .withDefault(true))

    ,

    /**
     * Special key inside executor configuration which will be used to define if PDS
     * will reuse SecHub storage locations. When storage location is reused the PDS
     * executor will not upload job data to PDS again and PDS server will reuse the
     * SecHub storage directly.
     */
    PDS_SECHUB_STORAGE_PATH(new RuntimeEnvironmentKey(PDSDefaultParameterKeyConstants.PARAM_KEY_SECHUB_STORAGE_PATH,
            "This contains the sechub storage location when sechub storage shall be used. So PDS knows location - in combination with sechub job UUID reuse is possible")
                    .markGenerated()),

    /**
     * This is automatically given to PDS by SecHub (depending on type type. E.g.
     * for a webscan this will be used to identify the current webscan target URL to
     * start scanning.)
     */
    PDS_TARGET_URL(new RuntimeEnvironmentKey(PDSDefaultParameterKeyConstants.PARAM_KEY_SCAN_TARGET_URL,
            "This conains the sechub storage location when sechub storage shall be used. So PDS knows location - in combination with sechub job UUID reuse is possible")
                    .markGenerated())

    ;

    private RuntimeEnvironmentKey key;

    PDSConfigDataKeyProvider(RuntimeEnvironmentKey key) {
        this.key = key;
    }

    public RuntimeEnvironmentKey getKey() {
        return key;
    }

}
