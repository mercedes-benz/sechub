// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

/**
 * An enumeration with all currently known product identifiers. If one of the
 * products is no longer supported, the corresponding identifier should be
 * marked as deprecated (with a short description for the deprecation) but not
 * removed! Do also NOT rename enumeration entries!<br>
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
    @Deprecated
    NETSPARKER(ProductType.WEBSCAN),

    /**
     * Infrastructure scanner
     */
    @Deprecated
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

    /** Product delegation server - license scan */
    PDS_LICENSESCAN(ProductType.LICENSESCAN),

    /** Product delegation server - analytics */
    PDS_ANALYTICS(ProductType.ANALYTICS)

    ;

    private ProductType type;

    private ProductIdentifier(ProductType type) {
        this.type = type;
    }

    public ProductType getType() {
        return type;
    }

}
