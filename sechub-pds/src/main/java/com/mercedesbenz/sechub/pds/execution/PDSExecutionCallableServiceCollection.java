// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSGetJobStreamService;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;

@Component
public class PDSExecutionCallableServiceCollection {

    @Autowired
    PDSJobTransactionService jobTransactionService;

    @Autowired
    PDSWorkspaceService workspaceService;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    @Autowired
    PDSCheckJobStatusService jobStatusService;

    @Autowired
    PDSExecutionEnvironmentService environmentService;

    @Autowired
    ProcessHandlingDataFactory processHandlingDataFactory;

    @Autowired
    PDSGetJobStreamService pdsGetJobStreamService;

    public PDSJobTransactionService getJobTransactionService() {
        return jobTransactionService;
    }

    public PDSWorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    public PDSProcessAdapterFactory getProcessAdapterFactory() {
        return processAdapterFactory;
    }

    public PDSCheckJobStatusService getJobStatusService() {
        return jobStatusService;
    }

    public PDSExecutionEnvironmentService getEnvironmentService() {
        return environmentService;
    }

    public ProcessHandlingDataFactory getProcessHandlingDataFactory() {
        return processHandlingDataFactory;
    }

    public PDSGetJobStreamService getPdsGetJobStreamService() {
        return pdsGetJobStreamService;
    }

}
