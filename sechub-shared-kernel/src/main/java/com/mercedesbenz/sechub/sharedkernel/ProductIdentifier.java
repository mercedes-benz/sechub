// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.ScanType;

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
    NETSPARKER(ScanType.WEB_SCAN),

    /**
     * Infrastructure scanner
     */
    @Deprecated
    NESSUS(ScanType.INFRA_SCAN),

    /**
     * SERECO = Security report collector
     */
    SERECO(ScanType.REPORT),

    /**
     * Static code analysis
     */
    CHECKMARX(ScanType.CODE_SCAN),

    /** Product delegation server - code scan */
    PDS_CODESCAN(ScanType.CODE_SCAN),

    /** Product delegation server - web scan */
    PDS_WEBSCAN(ScanType.WEB_SCAN),

    /** Product delegation server - infrastructure scan execution */
    PDS_INFRASCAN(ScanType.INFRA_SCAN),

    /** Product delegation server - license scan */
    PDS_LICENSESCAN(ScanType.LICENSE_SCAN),

    /** Product delegation server - secret scan */
    PDS_SECRETSCAN(ScanType.SECRET_SCAN),

    /** Product delegation server - iac scan */
    PDS_IACSCAN(ScanType.IAC_SCAN),

    /** Product delegation server - analytics */
    PDS_ANALYTICS(ScanType.ANALYTICS),

    /** Product delegation server - prepare */
    PDS_PREPARE(ScanType.PREPARE),

    UNKNOWN(ScanType.UNKNOWN);

    ;

    private ScanType type;

    private ProductIdentifier(ScanType type) {
        this.type = type;
    }

    public ScanType getType() {
        return type;
    }

    public static ProductIdentifier fromString(String productIdentifier) {
        for (ProductIdentifier productId : ProductIdentifier.values()) {
            if (productId.name().equalsIgnoreCase(productIdentifier)) {
                return productId;
            }

        }

        return UNKNOWN;
    }
}
