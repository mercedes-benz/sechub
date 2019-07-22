// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.integrationtest.api.AbstractTestExecutable;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;

public class ScenarioInitializer {

	private static final int DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION = 3;
	private static final Logger LOG = LoggerFactory.getLogger(ScenarioInitializer.class);

	ScenarioInitializer() {

	}

	public ScenarioInitializer createProject(TestProject project, TestUser owner) {
		TestAPI.as(TestAPI.SUPER_ADMIN).createProject(project,owner.getUserId());
		return this;
	}

	/**
	 * Creates an user, updates the token
	 *
	 * @param user
	 * @return
	 */
	public ScenarioInitializer createUser(TestUser user) {
		LOG.info("create user:{}",user);
		assertUser(user).doesNotExist();

		as(ANONYMOUS).signUpAs(user);
		as(SUPER_ADMIN).acceptSignup(user);

		assertUser(user).doesExist();

		/* execute receive of new api token */
		String link = getLinkToFetchNewAPITokenAfterSignupAccepted(user);
		assertFalse(link.isEmpty());
		udpdateAPITokenByOneTimeTokenLink(user, link);

		return this;
	}

//	public ScenarioInitializer waitUntilUserCanAccessProject(TestUser user, TestProject project) {
//		return waitUntilUserCanAccessProject(user,project, DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
//	}
//
//	@SuppressWarnings("unchecked")
//	public ScenarioInitializer waitUntilUserCanAccessProject(TestUser user, TestProject project, int seconds) {
//		TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(user,seconds,HttpClientErrorException.class) {
//			@Override
//			public boolean runImpl() throws Exception {
//				assertUser(user).can(project);
//				return true;
//			}
//		});
//		return this;
//	}

	public ScenarioInitializer waitUntilUserCanLogin(TestUser user) {
		return waitUntilUserCanLogin(user,DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
	}

	@SuppressWarnings("unchecked")
	public ScenarioInitializer waitUntilUserCanLogin(TestUser user, int seconds) {
		TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(user,seconds,HttpClientErrorException.class) {
			@Override
			public boolean runImpl() throws Exception {
				assertUser(user).canLogin();
				return true;
			}
		});
		return this;
	}

	public ScenarioInitializer waitUntilUserExists(TestUser user) {
		return waitUntilUserExists(user,DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
	}

	@SuppressWarnings("unchecked")
	public ScenarioInitializer waitUntilUserExists(TestUser user, int seconds) {
		TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN,seconds,HttpClientErrorException.class) {
			@Override
			public boolean runImpl() throws Exception {
				assertUser(user).doesExist();
				return true;
			}
		});
		return this;
	}

	public ScenarioInitializer waitUntilProjectExists(TestProject project) {
		return waitUntilProjectExists(project, DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
	}

	@SuppressWarnings("unchecked")
	public ScenarioInitializer waitUntilProjectExists(TestProject project, int seconds) {
		TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN,seconds,HttpClientErrorException.class) {
			@Override
			public boolean runImpl() throws Exception {
				assertProject(project).doesExist();
				return true;
			}
		});
		return this;
	}

	public void assignUserToProject(TestProject project, TestUser targetUser) {
		TestAPI.as(SUPER_ADMIN).assignUserToProject(targetUser, project);
	}

}