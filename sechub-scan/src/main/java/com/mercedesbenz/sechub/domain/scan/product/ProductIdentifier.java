// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.sharedkernel.MustBeKeptStable;

/**
 * An enumeration with all currently known products identifiers. If one of the
 * products is no longer supported the identifiers should be marked deprecated
 * but not removed! Do also NOT rename those enums!<br>
 * <br>
 * REASON: We use the names inside database for results and as identifiers! So
 * keep as is!
 *
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable("The enum is used as identifiers in database. Do NOT rename it or remove values. Mark older products as deprecated!")
public enum ProductIdentifier {

    /**
     * Webscanner
     */
    NETSPARKER(ProductType.WEBSCAN),

    /**
     * Infrastructure scanner
     */
    NESSUS(ProductType.INFRASCAN),

    /**
     * SERECO = Security report collector
     */
    SERECO(ProductType.REPORT_COLLECTOR),

    /**
     * Static code analysis
     */
    CHECKMARX(ProductType.CODESCAN),

    /** Product delegation server - code scan */
    PDS_CODESCAN(ProductType.CODESCAN),

    /** Product delegation server - web scan */
    PDS_WEBSCAN(ProductType.WEBSCAN),

    /** Product delegation server - infrastructure scan execution */
    PDS_INFRASCAN(ProductType.INFRASCAN),

    ;

    private ProductType type;

    private ProductIdentifier(ProductType type) {
        this.type = type;
    }

    public ProductType getType() {
        return type;
    }

}
