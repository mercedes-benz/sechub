// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.AssertSecHubReport.assertSecHubReport;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.api.ExecutionConstants;
import com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation;
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestDataConstants;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectChangeAccessLevelScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(60);

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    /* @formatter:off */
    @Test
    public void a_delete_removes_former_access_level_settings() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);
        assertProject(project).hasAccessLevel(ProjectAccessLevel.NONE);
        
        /* execute */
        as(SUPER_ADMIN).deleteProject(PROJECT_1);
        // now we create a new project with same name etc.
        as(SUPER_ADMIN).
            createProject(PROJECT_1, USER_1.getUserId()).
            addProjectsToProfile(ExecutionConstants.DEFAULT_EXECUTION_PROFILE_ID, PROJECT_1).
            assignUserToProject(USER_1, PROJECT_1);
        
        /* test*/
        // now we test that the acces level is full... and not NONE as before the delete...
        
        assertProject(project).hasAccessLevel(ProjectAccessLevel.FULL);
        // we start a job by USER1 and download the results- at this moment, this is possible, because project access level of new projectis "FULL"
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result).
            hasTrafficLight(TrafficLight.YELLOW);
        
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void admin_changes_project_state_to_no_access_project_details_contains_the_information() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);

        /* test */
        assertProject(project).hasAccessLevel(ProjectAccessLevel.NONE);
    }
    /* @formatter:on */

    /* @formatter:off */
	@Test
	public void get_job_status__existing_job_read_access_level_changing_test_different_access_levels() throws Exception {
        /* prepare + test preconditions */
	    TestProject project = PROJECT_1;
	    
	    // we start a job by USER1 - at this moment, this is possible, because project access level is "FULL"
        UUID jobUUID = as(USER_1).createCodeScan(project,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__FAST);

        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test 1 */
        as(USER_1).getJobStatus(project, jobUUID);
        
        
        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);

        /* test 2 */
        expectHttpFailure(()->{
            as(USER_1).getJobStatus(project, jobUUID);
        }, HttpStatus.FORBIDDEN);

        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.FULL);
        
        /* test 1 */
        as(USER_1).getJobStatus(project, jobUUID);

	}
	/* @formatter:on */

    /* @formatter:off */
    @Test
    public void get_job_report__existing_job_read_access_level_changing_test_different_access_levels() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        
        // we start a job by USER1 - at this moment, this is possible, because project access level is "FULL"
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result).
            finding().id(1).name("Absolute Path Traversal").isContained().
            hasTrafficLight(TrafficLight.YELLOW);

        UUID jobUUID = result.getSechubJobUUID();

        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test 1 */
        as(USER_1).getJobReport(project, jobUUID);
        
        
        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);

        /* test 2 */
        expectHttpFailure(()->{
            as(USER_1).getJobReport(project, jobUUID);
        }, HttpStatus.FORBIDDEN);
        // even as an administrator, using same rest api
        // the report cannot be fetched 
        expectHttpFailure(()->{
            as(SUPER_ADMIN).getJobReport(project, jobUUID);
        }, HttpStatus.FORBIDDEN);
        
        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.FULL);
        
        /* test 1 */
        as(USER_1).getJobReport(project, jobUUID);

    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void read_only___user_1_cannot_create_new_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test */
        expectHttpFailure(()->{
            as(USER_1).createWebScan(project);
        }, HttpStatus.FORBIDDEN);

    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void read_only___user_1_cannot_approve_existing_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createWebScan(project);
        
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test */
        expectHttpFailure(()->{
            as(USER_1).approveJob(project, jobUUID);
        }, HttpStatus.FORBIDDEN);

    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void read_only___user_1_cannot_upload_sourcecode_to_existing_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__FAST);
        
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test */
        expectHttpFailure(()->{
            as(USER_1).upload(project, jobUUID, TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
        }, HttpStatus.FORBIDDEN);

    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void none___user_1_cannot_create_new_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);
    
        /* test */
        expectHttpFailure(()->{
            as(USER_1).createWebScan(project);
        }, HttpStatus.FORBIDDEN);
    
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void none___user_1_cannot_approve_existing_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createWebScan(project);
        
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);
    
        /* test */
        expectHttpFailure(()->{
            as(USER_1).approveJob(project, jobUUID);
        }, HttpStatus.FORBIDDEN);
    
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void none___user_1_cannot_upload_sourcecode_to_existing_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__FAST);
        
        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);
    
        /* test */
        expectHttpFailure(()->{
            as(USER_1).upload(project, jobUUID, TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
        }, HttpStatus.FORBIDDEN);
    
    }
    /* @formatter:on */

}
