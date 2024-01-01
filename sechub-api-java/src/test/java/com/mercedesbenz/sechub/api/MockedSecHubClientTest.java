// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

class MockedSecHubClientTest {

    private MockedSecHubClient clientToTest;

    @BeforeEach
    void beforeEach() {
        clientToTest = new MockedSecHubClient();
    }

    @Test
    void mock_initial_behavior() throws Exception {

        /* test */
        assertTrue(clientToTest.fetchAllExecutorConfigurationInfo().isEmpty());
        assertTrue(clientToTest.fetchAllOpenSignups().isEmpty());
        assertTrue(clientToTest.fetchAllProjectIds().isEmpty());
        assertTrue(clientToTest.fetchAllUserIds().isEmpty());

    }

    @Test
    void mock_jobs() throws Exception {
        /* prepare */
        SecHubConfigurationModel configuration = new SecHubConfigurationModel();
        configuration.setProjectId("project1");

        /* execute */
        UUID uuid = clientToTest.createJob(configuration);

        /* test */
        assertNotNull(uuid);
        JobStatus jobStatus = clientToTest.fetchJobStatus("project1", uuid);
        jobStatus.getState().equals(ExecutionState.INITIALIZING);
    }

    @Test
    void mock_project() throws Exception {
        Project project = new Project();
        project.setName("projectname");

        /* execute */
        clientToTest.createProject(project);

        /* test */
        List<String> allProjects = clientToTest.fetchAllProjectIds();
        assertEquals(1, allProjects.size());
        assertTrue(allProjects.contains("projectname"));
    }

    @Test
    void mock_user_signup() throws Exception {
        /* execute */
        UserSignup signup = new UserSignup();
        signup.setUserId("somebody");
        signup.setEmailAddress("somebody@example.org");

        clientToTest.createSignup(signup);

        /* test */
        List<OpenUserSignup> signups = clientToTest.fetchAllOpenSignups();
        assertEquals(1, signups.size());
        assertTrue(clientToTest.fetchAllUserIds().isEmpty());

        /* execute 2 */
        clientToTest.acceptOpenSignup("somebody");
        assertTrue(clientToTest.fetchAllOpenSignups().isEmpty());

        /* test 2 */
        List<String> userIds = clientToTest.fetchAllUserIds();
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains("somebody"));

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void mock_profiles_and_configurations(boolean enabled) throws Exception {

        /* check precondition */
        assertFalse(clientToTest.isExecutionProfileExisting("testprofile"));

        /* execute */
        ExecutionProfileCreate create = new ExecutionProfileCreate();
        create.setDescription("d1");
        create.setEnabled(enabled);
        clientToTest.createExecutionProfile("testprofile", create);

        /* execute 2 */
        ExecutorConfiguration config = new ExecutorConfiguration();
        config.setEnabled(enabled);
        UUID configUUID = clientToTest.createExecutorConfiguration(config);

        /* test */
        List<ExecutorConfigurationInfo> info = clientToTest.fetchAllExecutorConfigurationInfo();
        assertEquals(1, info.size());
        ExecutorConfigurationInfo configuration = info.iterator().next();
        assertEquals(enabled, configuration.isEnabled());

        clientToTest.addExecutorConfigurationToProfile(configUUID, "testprofile");

        /* test 2 */
        assertTrue(clientToTest.isExecutionProfileExisting("testprofile"));
        assertEquals(1, info.size());
        ExecutorConfigurationInfo profile = info.iterator().next();
        assertEquals(enabled, profile.isEnabled());

    }

}
