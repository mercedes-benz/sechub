package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.commons.model.ScanType;

public interface PrepareProductExecutionService extends ProductExecutionStoreService {

    default public ScanType getScanType() {
        return ScanType.PREPARE;
    }
}
