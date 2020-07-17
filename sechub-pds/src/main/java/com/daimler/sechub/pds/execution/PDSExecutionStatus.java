package com.daimler.sechub.pds.execution;

import java.util.ArrayList;
import java.util.List;

public class PDSExecutionStatus {
    
    public int queueMax;
    
    public int jobsInQueue;
    
    public List<PDSExecutionJobInQueueStatusEntry> entries = new ArrayList<>();

}
