// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.execution;

import java.util.UUID;

import com.daimler.sechub.pds.job.PDSJob;

public class PDSExecutionJobInQueueStatusEntry {

    public UUID jobUUID;
    
    public boolean done;

    public boolean canceled;
    
    public PDSJob job;
}
