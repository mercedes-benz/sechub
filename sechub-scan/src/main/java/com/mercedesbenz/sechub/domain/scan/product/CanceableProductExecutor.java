// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

public interface CanceableProductExecutor extends ProductExecutor {

    public boolean cancel(ProductExecutorData data) throws Exception;
}
