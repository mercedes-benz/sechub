// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;

/**
 * Strategy to check if a IaC scan vulnerability identified by a product is
 * handled by a false positive meta data configuration
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class IacScanJobDataFalsePositiveStrategy extends AbstractSourceCodeBasedFalsePositiveStrategy {

    @Override
    protected ScanType getScanType() {
        return ScanType.IAC_SCAN;
    }

}
