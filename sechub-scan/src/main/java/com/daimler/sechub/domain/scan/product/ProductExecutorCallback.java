// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import com.daimler.sechub.adapter.AdapterMetaDataCallback;

public interface ProductExecutorCallback extends AdapterMetaDataCallback{

    public abstract ProductResult getProductResult();
    
    public void setCurrentProductResult(ProductResult result);
    
    public ProductResult save(ProductResult result);
    
    public AdapterMetaDataConverter getMetaDataConverter();
    
}
