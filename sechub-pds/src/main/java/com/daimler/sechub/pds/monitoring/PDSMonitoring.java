package com.daimler.sechub.pds.monitoring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.daimler.sechub.pds.job.PDSJobStatusState;

public class PDSMonitoring {

    public Map<PDSJobStatusState, Integer> jobs = new LinkedHashMap<>();
    
    public List<PDSClusterMember> clusterMembers = new ArrayList<>();
    
}
