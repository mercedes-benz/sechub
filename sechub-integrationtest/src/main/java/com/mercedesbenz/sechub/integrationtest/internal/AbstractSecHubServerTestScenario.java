// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;

/**
 * Abstract base scenario implementation
 *
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractSecHubServerTestScenario implements SecHubServerTestScenario {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSecHubServerTestScenario.class);

    private static Map<Class<? extends AbstractSecHubServerTestScenario>, List<TestUser>> createdTestUsersMap = new HashMap<>();
    private static Map<Class<? extends AbstractSecHubServerTestScenario>, List<TestProject>> createdTestProjectsMap = new HashMap<>();
    private int tempIdCounter;

    /**
     * Create a test project <b>instance</b> inside a scenario. But will not create
     * the real project on database etc. This is only the definition for a scenario.
     * The scenario itself will cleanup and handle the test project instance!
     *
     * <br>
     * Also white list entries are automatically added to this test project
     *
     * @param clazz
     * @param userIdPart
     * @return
     */
    protected static TestProject createTestProject(Class<? extends AbstractSecHubServerTestScenario> clazz, String projectIdPart) {
        return createTestProject(clazz, projectIdPart, true);
    }

    /**
     * Create a test project <b>instance</b> inside a scenario. But will not create
     * the real project on database etc. This is only the definition for a scenario.
     * The scenario itself will cleanup and handle the test project instance!
     *
     * @param clazz
     * @param userIdPart
     * @param createWhiteList when <code>true</code> white list entries are
     *                        automatically created for long running and normal time
     *                        consuming test calls (means fast)
     * @return
     */
    protected static TestProject createTestProject(Class<? extends AbstractSecHubServerTestScenario> clazz, String projectIdPart, boolean createWhiteList) {
        TestProject testProject = new TestProject(projectIdPart, createWhiteList);

        List<TestProject> testProjects = getTestProjects(clazz);
        testProjects.add(testProject);

        return testProject;
    }

    /**
     * Create a test user <b>instance</b> inside a scenario. But will not create the
     * real user on database etc. This is only the definition for a scenario. The
     * scenario itself will cleanup and handle the test user instance!
     *
     * @param clazz
     * @param userIdPart
     * @return
     */
    protected static TestUser createTestUser(Class<? extends AbstractSecHubServerTestScenario> clazz, String userIdPart) {
        TestUser testUser = new TestUser(userIdPart, null);

        List<TestUser> testUsers = getTestUsers(clazz);
        testUsers.add(testUser);

        return testUser;
    }

    private static List<TestUser> getTestUsers(Class<? extends AbstractSecHubServerTestScenario> clazz) {
        return createdTestUsersMap.computeIfAbsent(clazz, key -> new ArrayList<>());
    }

    private static List<TestProject> getTestProjects(Class<? extends AbstractSecHubServerTestScenario> clazz) {
        return createdTestProjectsMap.computeIfAbsent(clazz, key -> new ArrayList<>());
    }

    protected ScenarioInitializer initializer() {
        return new ScenarioInitializer();
    }

    protected void cleanupAllTestUsers() {
        List<TestUser> testusers = getTestUsers(getClass());
        LOG.debug("CLEANUP all test uses");

        for (TestUser user : testusers) {
            LOG.trace("Drop user:{}", user);
            resetUserInstanceData(user);
            dropExistingUser(user);
            dropExistingSignups(user);
        }

    }

    public void prepareTestData() {
        List<TestUser> testUsers = getTestUsers(getClass());
        for (TestUser user : testUsers) {
            LOG.debug("recalculate user:{}", user);
            user.prepare(this);
        }
        List<TestProject> testProjects = getTestProjects(getClass());
        for (TestProject project : testProjects) {
            LOG.debug("recalculate project:{}", project);
            project.prepare(this);
        }
    }

    protected TestRestHelper getRestHelper() {
        TestRestHelper restHelper = getContext().getTemplateForSuperAdmin();
        return restHelper;
    }

    protected RestTemplate getRestTemplate() {
        return getRestHelper().getTemplate();
    }

    protected void cleanupAllTestProjects() {
        LOG.debug("CLEANUP all test projects");
        List<TestProject> testProjects = getTestProjects(getClass());
        for (TestProject project : testProjects) {
            LOG.debug("drop project:{}", project);
            resetProjectInstanceData(project);
            dropExistingProject(project);
        }

    }

    private void resetProjectInstanceData(TestProject project) {
        // currently nothing
    }

    private void resetUserInstanceData(TestUser user) {
        // reset tokens (which can be changed by runtime)
        user.updateToken(null);
    }

    private void dropExistingSignups(TestUser user) {

        try {
            getRestTemplate().delete(getContext().getUrlBuilder().buildAdminDeletesUserSignUpUrl(user.getUserId()));
        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            if (HttpStatus.SC_NOT_FOUND != statusCode) {
                throw new IllegalStateException("Cannot delete user signup:" + user.getUserId() + ", http status=" + statusCode, e);
            } else {
                /* ok did not exist before... so no delete possible but still okay */
            }
        }
        TestAPI.assertUser(user).doesNotExist();
    }

    private void dropExistingProject(TestProject project) {
        try {
            getRestTemplate().delete(getContext().getUrlBuilder().buildDeleteProjectUrl(project.getProjectId()));
        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            if (HttpStatus.SC_NOT_FOUND != statusCode) {
                throw new IllegalStateException("Cannot delete test project:" + project.getProjectId() + ", http status=" + statusCode, e);
            } else {
                /* ok did not exist before... so no delete possible but still okay */
            }
        }
        TestAPI.assertProject(project).doesNotExist(9); // we try maximum 9x330 millis = approx. 3 seconds
    }

    private void dropExistingUser(TestUser user) {
        try {
            getRestTemplate().delete(getContext().getUrlBuilder().buildDeleteUserUrl(user.getUserId()));
        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            if (HttpStatus.SC_NOT_FOUND != statusCode) {
                throw new IllegalStateException("Cannot delete test user:" + user.getUserId() + ", http status=" + statusCode, e);
            } else {
                /* ok did not exist before... so no delete possible but still okay */
            }
        }
        TestAPI.assertUser(user).doesNotExist(3); // we try maximum 3 times (waits 1 second)
    }

    @Override
    public final void prepare(String testClass, String testMethod) {
        prepareImpl();
    }

    @Override
    public TestProject newTestProject(String projectIdPart) {
        if (this instanceof StaticTestScenario) {
            throw new IllegalStateException("A static test scenario does not have temp projects! static means 'does not change' ...");
        }
        TestProject project = createTestProject(getClass(), projectIdPart);
        project.prepare(this);
        return project;
    }

    @Override
    public TestProject newTestProject() {
        tempIdCounter++;
        return newTestProject("tmp_" + tempIdCounter);
    }

    protected final void prepareImpl() {

        boolean resetEventInspection = true;
        boolean resetMails = true;
        boolean cleanNecessary = true;
        boolean initializeNecessary = true;

        if (this instanceof StaticTestScenario) {
            StaticTestScenario sts = (StaticTestScenario) this;
            initializeNecessary = sts.isInitializationNecessary();
            resetEventInspection = sts.isEventResetNecessary();
            resetMails = sts.isEmailResetNecessary();
            cleanNecessary = initializeNecessary;
        } else if (this instanceof CleanScenario) {
            initializeNecessary = true;
            cleanNecessary = true;
        } else if (this instanceof GrowingScenario) {
            initializeNecessary = true;
            cleanNecessary = false;

            GrowingScenario growingScenario = (GrowingScenario) this;
            growingScenario.grow();

        }

        prepareTestData();

        if (resetEventInspection) {
            resetAndStopEventInspection();
        }
        if (resetMails) {
            resetEmails();
        }
        if (cleanNecessary) {
            LOG.info("## [CLEAN] remove old test data");
            cleanupTestdataAfterTest();
        } else {
            LOG.info("## [CLEAN] skipped");
        }

        if (initializeNecessary) {
            LOG.info("## [INIT ] trigger test data initialization on server side");
            initializeTestData();
            LOG.info("## [WAIT ] for all test data availale");
            waitForTestDataAvailable();
        } else {
            LOG.info("## [INIT ] skipped");
        }

    }

    private void cleanupTestdataAfterTest() {
        cleanupAllTestProjects();
        cleanupAllTestUsers();
    }

    protected void resetAndStopEventInspection() {
        getRestHelper().post(getContext().getUrlBuilder().buildIntegrationTestResetAndStopEventInspection());
    }

    protected abstract void initializeTestData();

    protected abstract void waitForTestDataAvailable();

    protected void resetEmails() {
        LOG.info("## [MAIL ] RESET mail mock data");
        getContext().emailAccess().reset();
    }

    private IntegrationTestContext getContext() {
        return IntegrationTestContext.get();
    }

}
