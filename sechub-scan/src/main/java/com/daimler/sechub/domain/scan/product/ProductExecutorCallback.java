// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import com.daimler.sechub.adapter.AdapterMetaDataCallback;

public interface ProductExecutorCallback extends AdapterMetaDataCallback{

    /**
     * Will return current product result. If none available a new one will be created.
     * @return the product result, never <code>null</code>
     */
    public abstract ProductResult getProductResult();
    
    /**
     * Set the current product result for callback
     * @param result
     */
    public void setCurrentProductResult(ProductResult result);
    
    /**
     * Saves the given product result
     * @param result
     * @return result entity or <code>null</code> (when given result parameter was <code>null</code> ) 
     */
    public ProductResult save(ProductResult result);
    
    /**
     * Resolves meta data converter
     * @return meta data converter
     */
    public AdapterMetaDataConverter getMetaDataConverter();
    
}
