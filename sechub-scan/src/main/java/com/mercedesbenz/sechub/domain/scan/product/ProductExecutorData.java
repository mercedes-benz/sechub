package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;
import java.util.Set;

import com.mercedesbenz.sechub.domain.scan.NetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetDataProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetDataSuppport;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

public class ProductExecutorData {

    List<NetworkTargetInfo> networkTargetInfoList;
    NetworkTargetDataProvider networkTargetDataProvider;

    ProductExecutorContext productExecutorContext;
    SecHubExecutionContext sechubExecutionContext;
    UUIDTraceLogID traceLogId;
    NetworkTargetDataSuppport networkTargetDataSupport;

    NetworkLocationProvider networkLocationProvider;
    NetworkTargetInfo currentNetworkTargetInfo;
    String traceLogIdAsString;
    Set<String> codeUploadFileSytemFolderPathes;

    
    ProductExecutorData(){
    }

    public NetworkTargetDataSuppport getNetworkTargetDataSupport() {
        return networkTargetDataSupport;
    }

    public void setNetworkTargetDataProvider(NetworkTargetDataProvider networkTargetDataProvider) {
        this.networkTargetDataProvider = networkTargetDataProvider;
    }

    public NetworkTargetDataProvider getNetworkTargetDataProvider() {
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
        return codeUploadFileSytemFolderPathes;
    }

    public String getTraceLogIdAsString() {
       return traceLogIdAsString;
    }

}
