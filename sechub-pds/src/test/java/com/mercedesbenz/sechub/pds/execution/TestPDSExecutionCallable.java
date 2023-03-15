// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPDSExecutionCallable extends PDSExecutionCallable {

    private static final Logger LOG = LoggerFactory.getLogger(TestPDSExecutionCallable.class);

    private long waitMillis;
    private PDSExecutionResult result;

    public TestPDSExecutionCallable(UUID jobUUID, long waitMillis, PDSExecutionResult result, PDSExecutionCallableServiceCollection serviceCollection) {
        super(jobUUID, serviceCollection);
        this.waitMillis = waitMillis;
        this.result = result;
    }

    @Override
    public PDSExecutionResult call() throws Exception {
        long millis = waitMillis;
        LOG.info("waiting {} ms-START", millis);
        Thread.sleep(millis);
        LOG.info("waiting {} ms-DONE", millis);
        return result;
    }

    @Override
    boolean prepareForCancel(boolean mayInterruptIfRunning) {
        return true;
    }

}