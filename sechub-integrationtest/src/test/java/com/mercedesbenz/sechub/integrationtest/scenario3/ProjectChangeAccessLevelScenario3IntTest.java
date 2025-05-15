// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.AssertReportUnordered.assertReportUnordered;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.AbstractTestExecutable;
import com.mercedesbenz.sechub.integrationtest.api.ExecutionConstants;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestJSONLocation;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode;
import com.mercedesbenz.sechub.integrationtest.api.JSonMessageHttpStatusExceptionTestValidator;
import com.mercedesbenz.sechub.integrationtest.api.TestDataConstants;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario3.class)
@Timeout(unit = TimeUnit.SECONDS, value = 60)
public class ProjectChangeAccessLevelScenario3IntTest {

    /* @formatter:off */
    @Test
    void none__test_delete_removes_former_access_level_settings() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);
        assertProject(project).hasAccessLevel(ProjectAccessLevel.NONE);

        /* execute */
        as(SUPER_ADMIN).deleteProject(project);

        /* test*/
        waitProjectDoesNotExist(project);

        // now we create a new project with same name etc.
        as(SUPER_ADMIN).
            createProject(project, USER_1).
            addProjectsToProfile(ExecutionConstants.DEFAULT_EXECUTION_PROFILE_ID, project);

        // now we test that the acces level is full... and not NONE as before the delete...
        assertProject(project).hasAccessLevel(ProjectAccessLevel.FULL);

        // we just start a job by USER1 and check if this possible again.
        // Because the administration domain must send the event to the scheduler domain.
        // and we can have race conditions here (means flaky tests). To avoid this
        // we use TestAPI#executeUntilSuccessOrTimeout
        executeUntilSuccessOrTimeout(new AbstractTestExecutable(USER_1,3,300, Forbidden.class, NotFound.class) {

            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
                return true;
            }

        });
    }
    /* @formatter:on */

    /* @formatter:off */
	@Test
	void get_job_status__existing_job_read_access_level_changing_test_different_access_levels() throws Exception {
        /* prepare + test preconditions */
	    TestProject project = PROJECT_1;

	    // we start a job by USER1 - at this moment, this is possible, because project access level is "FULL"
        UUID jobUUID = as(USER_1).createCodeScan(project,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__ZERO_WAIT);

        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test 1 */
        as(USER_1).getJobStatus(project, jobUUID);


        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);

        /* test 2 */
        expectHttpFailure(()->{
            as(USER_1).getJobStatus(project, jobUUID);
        }, new JSonMessageHttpStatusExceptionTestValidator(HttpStatus.FORBIDDEN, "Project "+project.getProjectId()+" does currently not allow read access."));

        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.FULL);

        /* test 1 */
        as(USER_1).getJobStatus(project, jobUUID);

	}
	/* @formatter:on */

    /* @formatter:off */
    @Test
    void get_job_report__existing_job_read_access_level_changing_test_different_access_levels() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;

        // we start a job by USER1 - at this moment, this is possible, because project access level is "FULL"
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW_ZERO_WAIT;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertReportUnordered(result).
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
        }, new JSonMessageHttpStatusExceptionTestValidator(HttpStatus.FORBIDDEN, "Project "+project.getProjectId()+" does currently not allow read access."));

        /* execute */ // we reuse the test, so we have not to create another job etc (reduce time cost)
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.FULL);

        /* test 1 */
        as(USER_1).getJobReport(project, jobUUID);

    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    void read_only___user_1_cannot_upload_sourcecode_to_existing_job_or_approve_it_or_create_new_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__ZERO_WAIT);

        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.READ_ONLY);

        /* test */
        expectHttpFailure(()->{
            as(USER_1).uploadSourcecode(project, jobUUID, TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
        }, HttpStatus.FORBIDDEN);

        expectHttpFailure(()->{
            as(USER_1).approveJob(project, jobUUID);
        }, HttpStatus.FORBIDDEN);

        expectHttpFailure(()->{
            as(USER_1).createWebScan(project);
        }, HttpStatus.FORBIDDEN);

        /* test 2 - invalid access levels */
        expectHttpFailure(() -> {
            as(SUPER_ADMIN).changeProjectAccessLevel(project, "INVALID_ACCESS_LEVEL");
        }, HttpStatus.BAD_REQUEST);
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    void none___user_1_cannot_upload_sourcecode_to_existing_job_or_approve_it_or_create_new_job() throws Exception {
        /* prepare + test preconditions */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__ZERO_WAIT);

        /* execute */
        as(SUPER_ADMIN).changeProjectAccessLevel(project,ProjectAccessLevel.NONE);

        /* test */
        expectHttpFailure(()->{
            as(USER_1).uploadSourcecode(project, jobUUID, TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
        }, HttpStatus.FORBIDDEN);

        expectHttpFailure(()->{
            as(USER_1).approveJob(project, jobUUID);
        }, HttpStatus.FORBIDDEN);

        expectHttpFailure(()->{
            as(USER_1).createWebScan(project);
        }, HttpStatus.FORBIDDEN);

    }
    /* @formatter:on */
}
