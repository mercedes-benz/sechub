package com.daimler.sechub.pds.execution;

import java.util.concurrent.Callable;

import com.daimler.sechub.pds.job.PDSJob;

class PDSExecutionCallable implements Callable<PDSExecutionCallResult> {
    private PDSJob pdsJob;

    public PDSExecutionCallable(PDSJob pdsJob) {
        this.pdsJob=pdsJob;
    }

    @Override
    public PDSExecutionCallResult call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}