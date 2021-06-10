// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.adapter.pds.PDSAdapterConstants;

/**
 * Contains key providers holding keys for executor configuration which are
 * interpreted <strong>at PDS side</strong>
 * 
 * @author Albert Tregnaghi
 *
 */
public enum PDSConfigDataKeyProvider implements PDSSecHubConfigDataKeyProvider<PDSConfigDataKey> {

    /**
     * Special key inside executor configuration which will be used to define the
     * PDS product identifier! So this key is not inside job parameters, but will be
     * available as "productID" for PDS
     */
    PDS_PRODUCT_IDENTIFIER(new PDSConfigDataKey(PDSAdapterConstants.PARAM_KEY_PRODUCT_IDENTIFIER,
            "Contains the product identifier, so PDS knows which part is to call on it's side.").markMandatory()),

    /**
     * This is automatically given to PDS by SecHub on every call by adapter
     */
    PDS_TARGET_TYPE(new PDSConfigDataKey(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE,
            "Contains the target type (depending on scan type) and will be just an additional information for PDS from SecHub.").markGenerated()),

    /**
     * Special key inside executor configuration which will be used to define if PDS
     * will reuse SecHub storage locations. When storage location is reused the PDS
     * executor will not upload job data to PDS again and PDS server will reuse the
     * SecHub storage directly.
     */
    PDS_USE_SECHUB_STORAGE(new PDSConfigDataKey(PDSAdapterConstants.PARAM_KEY_USE_SECHUB_STORAGE,
            "When 'true' the SecHub storage will be reused by PDS server. In this case SecHub will not upload job data to PDS.\n"
                    + "But it's crucial to have same root storage setup on PDS server side (e.g. same s3 bucket for S3 storage, or same NFS base for shared volumes).\n"
                    + "When not `true` or not defined, pds will use its own storage locations").markAlwaysSentToPDS())

    ,

    /**
     * Special key inside executor configuration which will be used to define if PDS
     * will reuse SecHub storage locations. When storage location is reused the PDS
     * executor will not upload job data to PDS again and PDS server will reuse the
     * SecHub storage directly.
     */
    PDS_SECHUB_STORAGE_PATH(new PDSConfigDataKey(PDSAdapterConstants.PARAM_KEY_SECHUB_STORAGE_PATH,
            "This conains the sechub storage location when sechub storage shall be used. So PDS knows location - in combination with sechub job UUID reuse is possible")
                    .markGenerated())

    ;

    private PDSConfigDataKey key;

    PDSConfigDataKeyProvider(PDSConfigDataKey key) {
        this.key = key;
    }

    public PDSConfigDataKey getKey() {
        return key;
    }

}
