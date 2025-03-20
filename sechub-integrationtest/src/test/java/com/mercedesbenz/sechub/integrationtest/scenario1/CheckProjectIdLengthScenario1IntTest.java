// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;
import static com.mercedesbenz.sechub.integrationtest.scenario1.Scenario1.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectIdValidationImpl;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario1.class)
public class CheckProjectIdLengthScenario1IntTest {

    private static final int PROJECT_ID_MAX_LENGTH = ProjectIdValidationImpl.PROJECTID_LENGTH_MAX;

    private static final int PROJECT_ID_MAX_LENGTH_PLUS_ONE = ProjectIdValidationImpl.PROJECTID_LENGTH_MAX + 1;

    @Test
    void can_create_project_with_project_id_having_maximum_length_and_execute_scan_using_sechub_cli() {
        /* prepare */
        TestUser user = OWNER_1;
        TestProject project = createTestProjectWithProjectIdLength(PROJECT_ID_MAX_LENGTH);

        /* execute + test 1 */
        as(SUPER_ADMIN).createProject(project, user);
        as(SUPER_ADMIN).addProjectsToProfile(PROFILE_1.id, project);

        /* test2 */
        // check client is able to handle the long project id as well
        ExecutionResult result = as(user).withSecHubClient().startAndWaitForCodeScan(project);
        assertExecutionResult(result).isGreen(); // result is as expected...
    }

    @Test
    void cannot_create_project_with_project_id_having_more_than_maximum_length() {
        /* prepare */
        TestUser user = OWNER_1;
        TestProject project = createTestProjectWithProjectIdLength(PROJECT_ID_MAX_LENGTH_PLUS_ONE);

        /* execute + test */
        expectHttpFailure(() -> {
            as(SUPER_ADMIN).createProject(project, user);
        }, HttpStatus.BAD_REQUEST);

    }

    private TestProject createTestProjectWithProjectIdLength(int projectIdLength) {
        String prefix = System.currentTimeMillis() + "_";
        String postfix = "x".repeat(projectIdLength - prefix.length());
        String projectIdPart = prefix + postfix;

        assertEquals(projectIdLength, projectIdPart.length());

        TestProject project = new TestProject(projectIdPart);
        project.setDescription("a small description"); // we want to keep the description small...
        return project;
    }

}
