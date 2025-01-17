// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionJobInQueueStatusEntry;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionStatus;
import com.mercedesbenz.sechub.pds.job.PDSJob;

public class TestPDSMonitoringDataUtil {

    public static final long JOB_STATUS_DONE = 5L;
    public static final long JOB_STATUS_RUNNING = 4L;
    public static final long JOB_STATUS_READYTOSTART = 2L;
    public static final long JOB_STATUS_QUEUED = 1L;
    public static final long JOB_STATUS_CREATED = 11L;
    public static final String SERVER_ID = "serverid1";
    public static final String MEMBER2_JOBA_OWNER = "developer";
    public static final String MEMBER2_IP = "192.168.178.24";
    public static final String MEMBER2_HOSTNAME = "hostname2";
    public static final String MEMBER1_IP = "192.168.178.23";
    public static final String MEMBER1_HOSTNAME = "hostname1";

    public static PDSMonitoring createTestMonitoringWith2ClusterMembers() {
        PDSMonitoring monitoringToTest = new PDSMonitoring();
        monitoringToTest.setServerId(SERVER_ID);
        Map<PDSJobStatusState, Long> map = new LinkedHashMap<>();
        map.put(PDSJobStatusState.CREATED, JOB_STATUS_CREATED);
        map.put(PDSJobStatusState.QUEUED, JOB_STATUS_QUEUED);
        map.put(PDSJobStatusState.READY_TO_START, JOB_STATUS_READYTOSTART);
        map.put(PDSJobStatusState.RUNNING, JOB_STATUS_RUNNING);
        map.put(PDSJobStatusState.DONE, JOB_STATUS_DONE);

        monitoringToTest.setJobs(map);
        PDSClusterMember member1 = new PDSClusterMember();
        member1.setHostname(MEMBER1_HOSTNAME);
        member1.setIp(MEMBER1_IP);
        member1.setExecutionState(new PDSExecutionStatus());

        PDSClusterMember member2 = new PDSClusterMember();
        member2.setHostname(MEMBER2_HOSTNAME);
        member2.setIp(MEMBER2_IP);
        member2.setExecutionState(new PDSExecutionStatus());
        PDSExecutionJobInQueueStatusEntry entry2a = new PDSExecutionJobInQueueStatusEntry();
        PDSJob job2a = new PDSJob();
        job2a.setStarted(LocalDateTime.now());
        job2a.setOwner(MEMBER2_JOBA_OWNER);
        job2a.setServerId(SERVER_ID);
        job2a.setState(PDSJobStatusState.RUNNING);
        entry2a.state = job2a.getState();
        entry2a.created = job2a.getCreated();
        entry2a.started = job2a.getStarted();

        member2.getExecutionState().entries.add(entry2a);

        monitoringToTest.getMembers().add(member1);
        monitoringToTest.getMembers().add(member2);
        return monitoringToTest;
    }
}
