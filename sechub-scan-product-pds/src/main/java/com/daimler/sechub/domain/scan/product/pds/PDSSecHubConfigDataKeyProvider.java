// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

public interface PDSSecHubConfigDataKeyProvider<T extends PDSSecHubConfigDataKey<?>> {

    public T getKey(); 
}
