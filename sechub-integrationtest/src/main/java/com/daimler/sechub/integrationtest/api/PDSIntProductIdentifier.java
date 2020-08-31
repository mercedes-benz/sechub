// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public enum PDSIntProductIdentifier {

    /* PDS see sechub-integration//src/main/resources/pds-config-integrationtest.json */
    PDS_INTTEST_CODESCAN("PDS_INTTEST_PRODUCT_CODESCAN"),
    
    PDS_INTTEST_INFRASCAN("PDS_INTTEST_PRODUCT_INFRASCAN"),
    
    PDS_INTTEST_WEBSCAN("PDS_INTTEST_PRODUCT_WEBSCAN"),
    
    ;

    private String id;

    private PDSIntProductIdentifier(String id) {
        this.id=id;
    }
    
    String getId() {
        return id;
    }
}
