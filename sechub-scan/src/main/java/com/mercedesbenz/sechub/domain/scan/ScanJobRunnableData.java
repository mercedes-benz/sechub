// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;

class ScanJobRunnableData {
    private SecHubExecutionContext executionContext;
    private SecHubExecutionException exception;
    private Thread scanJobExecutionThread;
    private Thread scanJobCancelThread;
    private UUID sechubJobUUID;
    private ProductExecutionServiceContainer executionServiceContainer;
    private List<ProductExecutor> runningProductExecutors = new ArrayList<>();

    ScanJobRunnableData(UUID sechubJobUUID, ProductExecutionServiceContainer executionServiceContainer, SecHubExecutionContext executionContext) {
        if (sechubJobUUID == null) {
            throw new IllegalArgumentException("sechub job uuid may not be null!");
        }
        this.sechubJobUUID = sechubJobUUID;
        if (executionServiceContainer == null) {
            throw new IllegalArgumentException("executionServiceContainer may not be null!");
        }
        this.executionServiceContainer = executionServiceContainer;

        if (executionContext == null) {
            throw new IllegalArgumentException("executionContext may not be null!");
        }
        this.executionContext = executionContext;

    }

    public void setException(SecHubExecutionException exception) {
        this.exception = exception;
    }

    public void setScanJobExecutionThread(Thread runnableThread) {
        this.scanJobExecutionThread = runnableThread;
    }

    public Thread getScanJobExecutionThread() {
        return scanJobExecutionThread;
    }

    public void setScanJobCancelThread(Thread cancelThread) {
        this.scanJobCancelThread = cancelThread;
    }

    public Thread getScanJobCancelThread() {
        return scanJobCancelThread;
    }

    public SecHubExecutionException getException() {
        return exception;
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }

    public UUID getSechubExecutioUUID() {
        return executionContext.getExecutionUUID();
    }

    public ProductExecutionServiceContainer getExecutionServiceContainer() {
        return executionServiceContainer;
    }

    public SecHubExecutionContext getExecutionContext() {
        return executionContext;
    }

    public List<ProductExecutor> getRunningProductExecutors() {
        return runningProductExecutors;
    }

}