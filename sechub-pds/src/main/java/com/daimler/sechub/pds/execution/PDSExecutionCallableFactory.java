package com.daimler.sechub.pds.execution;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.pds.job.PDSJobTransactionService;
import com.daimler.sechub.pds.job.PDSWorkspaceService;

@Component
public class PDSExecutionCallableFactory {

    @Autowired
    PDSJobTransactionService updateJobTransactionService;
    
    @Autowired
    PDSWorkspaceService workspaceService;
    
    @Autowired
    PDSExecutionEnvironmentService environmentService;

    public PDSExecutionCallable createCallable(UUID jobUUID) {
        return new PDSExecutionCallable(jobUUID, updateJobTransactionService, workspaceService,environmentService);
    }
}
