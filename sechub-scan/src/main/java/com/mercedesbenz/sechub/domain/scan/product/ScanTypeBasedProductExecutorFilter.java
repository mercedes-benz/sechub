// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class ScanTypeBasedProductExecutorFilter {

    private ScanType acceptedScanType;

    public ScanTypeBasedProductExecutorFilter(ScanType acceptedScanType) {
        if (acceptedScanType == null) {
            throw new IllegalArgumentException("accepted scan type may not be null!");
        }
        this.acceptedScanType = acceptedScanType;
    }

    public List<ProductExecutor> filter(List<ProductExecutor> productExecutors) {
        List<ProductExecutor> result = new ArrayList<>();
        if (productExecutors == null) {
            return result;
        }
        for (ProductExecutor executor : productExecutors) {
            if (acceptedScanType.equals(executor.getScanType())) {
                result.add(executor);
            }
        }
        return result;
    }

}
