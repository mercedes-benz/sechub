// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;

@Component
public class PDSExecutionCallableFactory {

    @Autowired
    PDSJobTransactionService updateJobTransactionService;

    @Autowired
    PDSCheckJobStatusService jobStatusService;

    @Autowired
    PDSWorkspaceService workspaceService;

    @Autowired
    PDSExecutionEnvironmentService environmentService;

    public PDSExecutionCallable createCallable(UUID jobUUID) {
        return new PDSExecutionCallable(jobUUID, updateJobTransactionService, workspaceService, environmentService, jobStatusService);
    }
}
