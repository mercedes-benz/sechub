package com.daimler.sechub.pds.monitoring;

import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.pds.execution.PDSExecutionJobInQueueStatusEntry;
import com.daimler.sechub.pds.execution.PDSExecutionStatus;
import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobStatusState;

public class PDSMonitoringTest {

    private PDSMonitoring monitoringToTest;

    @Before
    public void before() throws Exception {
        monitoringToTest = new PDSMonitoring();
    }

    @Test
    public void monitoring_toJSON() throws Exception {
        /* prepare */
        monitoringToTest.setServerId("serverid1");
        Map<PDSJobStatusState, Long> map = new LinkedHashMap<>();
        map.put(PDSJobStatusState.CREATED, 11L);
        map.put(PDSJobStatusState.QUEUED, 1L);
        map.put(PDSJobStatusState.READY_TO_START, 2L);
        map.put(PDSJobStatusState.RUNNING, 4L);
        map.put(PDSJobStatusState.DONE, 5L);
        
        monitoringToTest.setJobs(map);
        PDSClusterMember member1 = new PDSClusterMember();
        member1.setHostname("hostname1");
        member1.setIp("192.168.178.23");
        member1.setExecutionState(new PDSExecutionStatus());
        
        PDSClusterMember member2 = new PDSClusterMember();
        member2.setHostname("hostname2");
        member2.setIp("192.168.178.24");
        member2.setExecutionState(new PDSExecutionStatus());
        PDSExecutionJobInQueueStatusEntry entry2a = new PDSExecutionJobInQueueStatusEntry();
        PDSJob job2a = new PDSJob();
        job2a.setStarted(LocalDateTime.now());
        job2a.setOwner("developer");
        job2a.setServerId("serverid1");
        job2a.setState(PDSJobStatusState.RUNNING);
        entry2a.job=job2a;
        member2.getExecutionState().entries.add(entry2a);
        
        monitoringToTest.getMembers().add(member1);
        monitoringToTest.getMembers().add(member2);
        
        /* execute*/
        String json = monitoringToTest.toJSON();
        
        /* test */
        assertNotNull(json);
    }
    
    @Test
    public void monitoring_example1_from_doc_can_be_loaded_and_contains_values() throws Exception {
        /* prepare */
        File file = new File("./../sechub-doc/src/docs/asciidoc/documents/pds/pds-monitoring-result-example1.json");
        String json = FileUtils.readFileToString(file, "UTF-8");
        
        /* execute */
        PDSMonitoring monitoring = PDSMonitoring.fromJSON(json);
        
        /* test */
        assertNotNull(monitoring);
        assertEquals(5, monitoring.getJobs().size());
    }

}
