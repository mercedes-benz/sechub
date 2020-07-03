// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario4;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario4.Scenario4.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.PDSIntProductIdentifier;
import com.daimler.sechub.integrationtest.api.TestProject;

/**
 * Integration test directly using REST API of integration test PDS (means without sechub).
 * When these tests fail, sechub tests will also fail, because PDS API corrupt or PDS server not alive
 * 
 * @author Albert Tregnaghi
 *
 */
public class DirectPDSAPIScenario4IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario4.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    public void pds_techuser_can_check_alive_state() {
        /* @formatter:off */
        /* execute */
        boolean alive = asPDS(PDS_TECH_USER).getIsAlive();
        
        /* test */
        assertTrue("PDS server is NOT alive !!! ILLEGAL STATE for tests: So all PDS related integration test will fail", alive);
        
        /* @formatter:on */
    }
    
    @Test
    public void pds_techuser_can_create_job_and_jobid_is_returned() {
        /* @formatter:off */
        /* prepare */

        UUID sechubJobUUID = UUID.randomUUID();
        
        /* execute */
        String result = asPDS(PDS_TECH_USER).createJobFor(sechubJobUUID, PDSIntProductIdentifier.PDS_INTTEST_CODESCAN);
        
        /* test */
        assertPDSJobCreateResult(result).hasJobUUID().getJobUUID();
        
        /* @formatter:on */
    }
    
    @Test
    public void pds_techuser_can_get_job_status_of_created_job_and_is_CREATED() {
        /* @formatter:off */
        /* prepare */

        UUID sechubJobUUID = UUID.randomUUID();
        
        String createResult = asPDS(PDS_TECH_USER).createJobFor(sechubJobUUID, PDSIntProductIdentifier.PDS_INTTEST_CODESCAN);
        UUID pdsJobUUID = assertPDSJobCreateResult(createResult).hasJobUUID().getJobUUID();

        /* execute */
        String result = asPDS(PDS_TECH_USER).getJobStatus(pdsJobUUID);
        
        /* test */
        assertPDSJobStatus(result).isInState("CREATED");
        /* @formatter:on */
    }
    
    @Test
    public void pds_techuser_can_upload_content_to_PDS() {
        /* @formatter:off */
        /* prepare */

        UUID sechubJobUUID = UUID.randomUUID();
        
        String createResult = asPDS(PDS_TECH_USER).createJobFor(sechubJobUUID, PDSIntProductIdentifier.PDS_INTTEST_CODESCAN);
        UUID pdsJobUUID = assertPDSJobCreateResult(createResult).hasJobUUID().getJobUUID();

        /* execute */
        asPDS(PDS_TECH_USER).upload(pdsJobUUID, "sourcecode.zip", "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip");
        
        /* test */
        assertPDSWorkspace().hasUploadedFile(pdsJobUUID, "sourcecode.zip");
        
        /* @formatter:on */
    }
    
    @Test
    public void pds_techuser_can_mark_job_as_ready_to_start_and_after_while_job_result_is_returned() {
        /* @formatter:off */
        /* prepare */

        UUID sechubJobUUID = UUID.randomUUID();
        
        String createResult = asPDS(PDS_TECH_USER).createJobFor(sechubJobUUID, PDSIntProductIdentifier.PDS_INTTEST_CODESCAN);
        UUID pdsJobUUID = assertPDSJobCreateResult(createResult).hasJobUUID().getJobUUID();
        asPDS(PDS_TECH_USER).upload(pdsJobUUID, "sourcecode.zip", "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip");
        
        /* execute */
        asPDS(PDS_TECH_USER).markJobAsReadyToStart(pdsJobUUID);
        
        /* test */
        String report = asPDS(PDS_TECH_USER).getJobReport(pdsJobUUID);
        assertTrue(report.contains("CRITICAL"));
        
        /* @formatter:on */
    }

    
    


}
