// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;
import java.util.Set;

import com.mercedesbenz.sechub.domain.scan.NetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataSuppport;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

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
    Set<String> codeUploadFileSystemFolderPaths;

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

    public Set<String> getCodeUploadFileSystemFolders() {
        return codeUploadFileSystemFolderPaths;
    }

    public String getTraceLogIdAsString() {
        return traceLogIdAsString;
    }

}
