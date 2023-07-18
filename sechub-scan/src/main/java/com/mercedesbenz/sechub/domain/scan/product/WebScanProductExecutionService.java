// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.commons.model.ScanType;

public interface WebScanProductExecutionService extends ProductExecutionStoreService {

    default public ScanType getScanType() {
        return ScanType.WEB_SCAN;
    }
}