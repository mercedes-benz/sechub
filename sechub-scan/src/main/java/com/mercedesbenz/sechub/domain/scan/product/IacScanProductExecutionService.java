// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.commons.model.ScanType;

public interface IacScanProductExecutionService extends ProductExecutionStoreService {

    default public ScanType getScanType() {
        return ScanType.IAC_SCAN;
    }
}