package com.daimler.sechub.domain.scan.product;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.AdapterMetaDataCallback;

public class ProductExecutorContext {
    

    private static final Logger LOG = LoggerFactory.getLogger(ProductExecutorContext.class);

    List<ProductResult> formerResults = new ArrayList<>();
    List<ProductResult> results = new ArrayList<>();
    ProductExecutorCallback callback;

    public ProductExecutorContext(List<ProductResult> formerResults, ProductExecutorCallback callback) {
        this.callback=callback;
        this.formerResults=formerResults;
        
        useFirstFormerResult();
    }

    public AdapterMetaDataCallback getCallBack() {
        return callback;
    }

    private ProductResult getFormerProductResultOrNull() {
        if (formerResults.size() > 0) {
            return formerResults.iterator().next();
        }
        return null;
    }
    
    public void useFirstFormerResult() {
        callback.setCurrentProductResult(getFormerProductResultOrNull());
    }
    
    public void useFirstFormerResultHavingMetaData(String key, URI uri) {
        useFirstFormerResultHavingMetaData(key, ""+uri);
    }
    
    public void useFirstFormerResultHavingMetaData(String key, String value) {
        LOG.debug("use first former result with key:{},value:{}",key,value);
        
        for (ProductResult result: formerResults) {
            if (result==null) {
                continue;
            }
            String metaDataString = result.getMetaData();
            AdapterMetaData metaDataOrNull = callback.getMetaDataConverter().convertToMetaDataOrNull(metaDataString);
            if (metaDataOrNull != null && metaDataOrNull.hasValue(key, value)) {
                callback.setCurrentProductResult(result);
                return;
            }
        }
        /* not fond - ensure null*/
        callback.setCurrentProductResult(null);
    }
    
    public ProductResult getCurrentProductResult() {
        return callback.getProductResult();
    }

    public AdapterMetaData getCurrentMetaDataOrNull() {
        return callback.getMetaDataOrNull();
    }

    public void persist(ProductResult productResult) {
        callback.save(productResult);
    }
    
    
}
