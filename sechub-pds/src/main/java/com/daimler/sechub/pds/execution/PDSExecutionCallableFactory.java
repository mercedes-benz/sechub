package com.daimler.sechub.pds.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSUpdateJobTransactionService;

@Component
public class PDSExecutionCallableFactory {

    @Autowired
    PDSUpdateJobTransactionService updateJobTransactionService;

    public PDSExecutionCallable createCallable(PDSJob job) {
        return new PDSExecutionCallable(job, updateJobTransactionService);
    }
}
