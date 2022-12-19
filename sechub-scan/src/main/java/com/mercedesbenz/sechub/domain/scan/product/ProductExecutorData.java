// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;

import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.domain.scan.NetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataSuppport;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;

public class ProductExecutorData {

    List<NetworkTargetInfo> networkTargetInfoList;
    NetworkTargetProductServerDataProvider networkTargetDataProvider;

    ProductExecutorContext productExecutorContext;
    SecHubExecutionContext sechubExecutionContext;
    UUIDTraceLogID traceLogId;
    NetworkTargetProductServerDataSuppport networkTargetProductServerDataSupport;

    NetworkLocationProvider networkLocationProvider;
    NetworkTargetInfo currentNetworkTargetInfo;
    String traceLogIdAsString;
    String mockDataIdentifier;
    private AdapterConfig rememberedAdapterConfig;

    ProductExecutorData() {
    }

    public NetworkTargetProductServerDataSuppport getNetworkTargetProductServerDataSupport() {
        return networkTargetProductServerDataSupport;
    }

    public void setNetworkTargetDataProvider(NetworkTargetProductServerDataProvider networkTargetDataProvider) {
        this.networkTargetDataProvider = networkTargetDataProvider;
    }

    public NetworkTargetProductServerDataProvider getNetworkTargetDataProvider() {
        return networkTargetDataProvider;
    }

    public void setNetworkLocationProvider(NetworkLocationProvider networkLocationSupport) {
        this.networkLocationProvider = networkLocationSupport;
    }

    public NetworkTargetInfo getCurrentNetworkTargetInfo() {
        return currentNetworkTargetInfo;
    }

    public ProductExecutorContext getProductExecutorContext() {
        return productExecutorContext;
    }

    public SecHubExecutionContext getSechubExecutionContext() {
        return sechubExecutionContext;
    }

    public UUIDTraceLogID getTraceLogId() {
        return traceLogId;
    }

    /**
     * Returns mock data identifier (if necessary)
     *
     * @return identifier or <code>null</code>
     */
    public String getMockDataIdentifier() {
        return mockDataIdentifier;
    }

    public String getTraceLogIdAsString() {
        return traceLogIdAsString;
    }

    public void rememberAdapterConfig(AdapterConfig config) {
        this.rememberedAdapterConfig = config;
    }

    public AdapterConfig getRememberedAdapterConfig() {
        return rememberedAdapterConfig;
    }

    public void forgetRememberedAdapterConfig() {
        this.rememberedAdapterConfig = null;
    }

}
