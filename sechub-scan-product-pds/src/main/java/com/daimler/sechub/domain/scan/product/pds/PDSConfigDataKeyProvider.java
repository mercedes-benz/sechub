// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.adapter.pds.PDSAdapterConstants;

/**
 * Contains key providers holding keys for executor configuration which are interpreted <strong>at PDS side</strong>
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
    PDS_PRODUCT_IDENTIFIER(new PDSConfigDataKey("pds.config.productidentifier",
            "Contains the product identifier, so PDS knows which part is to call on it's side.").markMandatory()),

    /**
     * This is automatically given to PDS by SecHub on every call by adapter
     */
    PDS_TARGET_TYPE(new PDSConfigDataKey(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE,
            "Contains the target type (depending on scan type) and will be just an additional information for PDS from SecHub.").markGenerated()),

    ;

    private PDSConfigDataKey key;

    PDSConfigDataKeyProvider(PDSConfigDataKey key) {
        this.key = key;
    }

    public PDSConfigDataKey getKey() {
        return key;
    }

}
