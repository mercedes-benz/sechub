package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.adapter.pds.PDSAdapterConstants;

/**
 * Contains keys from executor configuration which are interpreted at PDS side
 * @author Albert Tregnaghi
 *
 */
public enum PDSOutputKeys implements PDSSecHubConfigDataKeyProvider<PDSOutputKey>{

    /**
     * Special key inside executor configuration which will be used to define the PDS product identifier!
     * So this key is not inside job parameters, but will be available as "productID" for PDS
     */
    PDS_PRODUCT_IDENTIFIER(new PDSOutputKey(PDSAdapterConstants.PARAM_KEY_PRODUCT_IDENTIFIER,"Contains the product identifier").markMandatory()),
    
    PDS_TARGET_TYPE(new PDSOutputKey(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE,"Contains the target key (depending on scan type)").markGenerated()), 
    
    ;

    private PDSOutputKey key;

    PDSOutputKeys(PDSOutputKey key) {
        this.key = key;
    }

    public PDSOutputKey getKey() {
        return key;
    }

}
