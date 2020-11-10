// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;

/**
 * An enumeration with all currently known products identifiers. If one of the
 * products is no longer supported the identifiers should be marked deprecated
 * but no removed! Do also NOT rename those enums!<br><br>
 * REASON: We use the names inside database for results and as identifiers! So keep as is! 
 *
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable("The enum is used as identifiers in database. Do NOT rename it or remove values. Mark older products as deprecated!")
public enum ProductIdentifier {

    /**
     * Webscanner
     */
    NETSPARKER,

    /**
     * Infrastructure scanner
     */
    NESSUS,

    /**
     * SERECO = Security report collector
     */
    SERECO,

    /**
     * Static code analysis
     */
    CHECKMARX,

    /** Product delegation server - code scan */
    PDS_CODESCAN,

    /** Product delegation server - web scan */
    PDS_WEBSCAN,

    /** Product delegation server - infrastructure scan execution */
    PDS_INFRASCAN,

    ;

    private ProductIdentifier() {

    }

}
