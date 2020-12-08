// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

/**
 * Special product identifiers used for PDS integration tests
 * @author Albert Tregnaghi
 *
 */
public enum PDSIntTestProductIdentifier {

    /* PDS see sechub-integration//src/main/resources/pds-config-integrationtest.json */
    PDS_INTTEST_CODESCAN("PDS_INTTEST_PRODUCT_CODESCAN"),
    
    PDS_INTTEST_INFRASCAN("PDS_INTTEST_PRODUCT_INFRASCAN"),
    
    PDS_INTTEST_WEBSCAN("PDS_INTTEST_PRODUCT_WEBSCAN"),
    
    ;

    private String id;

    private PDSIntTestProductIdentifier(String id) {
        this.id=id;
    }
    
    public String getId() {
        return id;
    }
}
