package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;

public class SecHubExecutionHistoryElement {

    private ProductExecutor productExecutor;
    private ProductExecutorData productExecutorData;
    private AdapterConfig adapterConfig;

    public void setProductExecutor(ProductExecutor productExecutor) {
        this.productExecutor = productExecutor;
    }

    public ProductExecutor getProductExecutor() {
        return productExecutor;
    }

    public void setProductExecutorData(ProductExecutorData data) {
        this.productExecutorData = data;
    }

    public ProductExecutorData getProductExecutorData() {
        return productExecutorData;
    }

    public void setAdapterConfig(AdapterConfig adapterConfig) {
        this.adapterConfig = adapterConfig;
    }

    public AdapterConfig getAdapterConfig() {
        return adapterConfig;
    }

}
