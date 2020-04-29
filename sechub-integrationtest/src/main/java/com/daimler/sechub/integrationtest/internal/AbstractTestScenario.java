// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.test.ExampleConstants;

/**
 * Abstract base scenario implementation
 *
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractTestScenario implements TestScenario {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTestScenario.class);

	private static Map<Class<? extends AbstractTestScenario>, List<TestUser>> createdTestUsersMap = new HashMap<>();
	private static Map<Class<? extends AbstractTestScenario>, List<TestProject>> createdTestProjectsMap = new HashMap<>();

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
	protected static TestProject createTestProject(Class<? extends AbstractTestScenario> clazz, String projectIdPart) {
		return createTestProject(clazz, projectIdPart,true);
	}
	/**
	 * Create a test project <b>instance</b> inside a scenario. But will not create
	 * the real project on database etc. This is only the definition for a scenario.
	 * The scenario itself will cleanup and handle the test project instance!
	 *
	 * @param clazz
	 * @param userIdPart
	 * @param createWhiteList when <code>true</code> white list entries are automatically created for long running
	 *  and normal time consuming test calls (means fast)
	 * @return
	 */
	protected static TestProject createTestProject(Class<? extends AbstractTestScenario> clazz, String projectIdPart, boolean createWhiteList) {
		String projectId = clazz.getSimpleName().toLowerCase() + "_" + projectIdPart;
		TestProject testProject;
		if (createWhiteList) {

			String whiteListURL = "http://locahost/" + projectId;
			List<String> whitelist= new ArrayList<>();
			whitelist.add(whiteListURL);

			for (IntegrationTestMockMode mode: IntegrationTestMockMode.values()) {
				if (mode.isTargetUsableAsWhitelistEntry()) {
					whitelist.add(mode.getTarget());
				}
			}

			// we additinally add long running url because its configured in webscan and for
			// infrascan mocks to have longer runs*/
			testProject = new TestProject(projectId, whitelist.toArray(new String[whitelist.size()]));
		}else {
			testProject = new TestProject(projectId);
		}

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
	protected static TestUser createTestUser(Class<? extends AbstractTestScenario> clazz, String userIdPart) {
		String userid = clazz.getSimpleName().toLowerCase() + "_" + userIdPart;
		TestUser testUser = new TestUser(userid, null, userid + "@"+ExampleConstants.URI_TARGET_SERVER);

		List<TestUser> testUsers = getTestUsers(clazz);
		testUsers.add(testUser);

		return testUser;
	}

	private static List<TestUser> getTestUsers(Class<? extends AbstractTestScenario> clazz) {
		return createdTestUsersMap.computeIfAbsent(clazz, key -> new ArrayList<>());
	}

	private static List<TestProject> getTestProjects(Class<? extends AbstractTestScenario> clazz) {
		return createdTestProjectsMap.computeIfAbsent(clazz, key -> new ArrayList<>());
	}

	@Override
	public String getName() {
		return getClass().getSimpleName().toLowerCase();
	}

	protected ScenarioInitializer initializer() {
		return new ScenarioInitializer();
	}

	protected void cleanupAllTestUsers() {
		List<TestUser> testusers = getTestUsers(getClass());
		LOG.debug("CLEANUP all test uses");

		for (TestUser user : testusers) {
			LOG.debug("Drop user:{}", user);
			resetUserInstanceData(user);
			dropExistingUser(user);
			dropExistingSignups(user);
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
	}

	@Override
	public final void prepare(String testClass, String testMethod) {
		String scenarioName = getClass().getSimpleName();
		LOG.info("############################################################################################################");
		LOG.info("###");
		LOG.info("### [START] Preparing scenario: '" + scenarioName+"'");
		LOG.info("###");
		LOG.info("############################################################################################################");
		LOG.info("###   Class ="+testClass);
		LOG.info("###   Method="+testMethod);
		LOG.info("############################################################################################################");

		prepareImpl();

		LOG.info("############################################################################################################");
		LOG.info("###");
		LOG.info("### [DONE ]  Preparing scenario: '" + scenarioName+"'");
		LOG.info("### [START]  Test itself");
		LOG.info("###");
		LOG.info("############################################################################################################");
		LOG.info("###   Class ="+testClass);
		LOG.info("###   Method="+testMethod);
		LOG.info("############################################################################################################");
		
		TestAPI.logInfoOnServer("\n\n\n"
		        + "  Start of integration test\n\n"
		        + "  - Test class:"+testClass+"\n"
		        + "  - Method:"+testMethod+"\n\n"
		        + "\n");
	}

	protected final void prepareImpl() {

	    boolean resetEventInspection=true;
	    boolean resetMails = true;
	    boolean initializeNecessary=true;
	    
	    if (this instanceof StaticTestScenario) {
	        StaticTestScenario sts = (StaticTestScenario) this;
	        initializeNecessary = sts.isInitializationNecessary();
	        resetEventInspection= sts.isEventResetNecessary();
	        resetMails=sts.isEmailResetNecessary();
	    }
	    
	    if (resetEventInspection) {
	        resetAndStopEventInspection();
	    }
	    if (resetMails) {
	        resetEmails();
	    }
		if (initializeNecessary) {
		    LOG.info("############################################################################################################");
		    LOG.info("## [CLEAN] remove old test data");
		    LOG.info("############################################################################################################");
		    cleanupTestdataAfterTest();
		}else {
		    LOG.info("############################################################################################################");
		    LOG.info("## [CLEAN] skipped");
		    LOG.info("############################################################################################################");
		}
		
		if (initializeNecessary) {
		    LOG.info("############################################################################################################");
		    LOG.info("## [INIT] trigger test data initialization on server side");
		    LOG.info("############################################################################################################");
		    initializeTestData();
		    LOG.info("############################################################################################################");
		    LOG.info("## [WAIT] for all test data availale");
		    LOG.info("############################################################################################################");
		    waitForTestDataAvailable();
		}else {
		    LOG.info("############################################################################################################");
            LOG.info("## [INIT] skipped");
            LOG.info("############################################################################################################");
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
		LOG.info("RESET mail mock data");
		getContext().emailAccess().reset();
	}

	private IntegrationTestContext getContext() {
		return IntegrationTestContext.get();
	}

}
