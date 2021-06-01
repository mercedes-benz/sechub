// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.integrationtest.api.AbstractTestExecutable;
import com.daimler.sechub.integrationtest.api.ExecutionConstants;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class ScenarioInitializer {

	private static final int DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION = 3;
	private static final Logger LOG = LoggerFactory.getLogger(ScenarioInitializer.class);
	

	public ScenarioInitializer createProject(TestProject project, TestUser owner) {
		TestAPI.as(TestAPI.SUPER_ADMIN).createProject(project,owner.getUserId());
		return this;
	}

	public ScenarioInitializer addProjectIdsToDefaultExecutionProfile_1(TestProject ...projects) {
	    TestAPI.as(TestAPI.SUPER_ADMIN).addProjectsToProfile(ExecutionConstants.DEFAULT_PROFILE_1_ID,projects);
	    return this;
	}
	
	public ScenarioInitializer addProjectIdsToDefaultExecutionProfile_2_PDS(TestProject ...projects) {
        TestAPI.as(TestAPI.SUPER_ADMIN).addProjectsToProfile(ExecutionConstants.DEFAULT_PROFILE_2_ID,projects);
        return this;
    }
	
	public ScenarioInitializer addProjectIdsToDefaultExecutionProfile_3_PDS_SARIF(TestProject ...projects) {
        TestAPI.as(TestAPI.SUPER_ADMIN).addProjectsToProfile(ExecutionConstants.DEFAULT_PROFILE_3_ID,projects);
        return this;
    }
	public ScenarioInitializer addProjectIdsToDefaultExecutionProfile_4_PDS_SARIF_NOT_USING_SECHUB_STORAGE(TestProject ...projects) {
	    TestAPI.as(TestAPI.SUPER_ADMIN).addProjectsToProfile(ExecutionConstants.DEFAULT_PROFILE_4_ID,projects);
	    return this;
	}
    
	
	public  ScenarioInitializer ensureDefaultExecutionProfile_1() {
	    return ensureDefaultExecutionProfile(IntegrationTestDefaultProfiles.PROFILE_1);
	}
	
	public  ScenarioInitializer ensureDefaultExecutionProfile_2_PDS_codescan() {
        return ensureDefaultExecutionProfile(IntegrationTestDefaultProfiles.PROFILE_2_PDS_CODESCAN);
    }
	
	public  ScenarioInitializer ensureDefaultExecutionProfile_3_PDS_codescan_sarif() {
        return ensureDefaultExecutionProfile(IntegrationTestDefaultProfiles.PROFILE_3_PDS_CODESCAN_SARIF);
    }
	
	public  ScenarioInitializer ensureDefaultExecutionProfile_4_PDS_codescan_sarif_no_sechub_storage_used() {
        return ensureDefaultExecutionProfile(IntegrationTestDefaultProfiles.PROFILE_4_PDS_CODESCAN_SARIF_NO_SECHUB_STORAGE_USED);
    }
	
	private  ScenarioInitializer ensureDefaultExecutionProfile(DoNotChangeTestExecutionProfile profile) {
	    if (TestAPI.canReloadExecutionProfileData(profile)){
	        return this;
	    }
	    Set<TestExecutorConfig> realConfigurations = new LinkedHashSet<>();

	    /* we iterate over initial list, where all defaults are inside - but UUID is null...*/
	    for (TestExecutorConfig config: profile.initialConfigurationsWithoutUUID) {
	        UUID uuid = TestAPI.as(SUPER_ADMIN).createProductExecutorConfig(config);
	        config.uuid=uuid; // set UUID from service to this so available 
	        realConfigurations.add(config);
	    }
	    /* define profile */
	    TestAPI.as(TestAPI.SUPER_ADMIN).createProductExecutionProfile(profile.id, profile);
	    TestExecutionProfile realProfile = TestAPI.as(TestAPI.SUPER_ADMIN).fetchProductExecutionProfile(profile.id);
	    
	    /* now we create real configurations having correct uuids etc. */
	    realProfile.configurations.addAll(realConfigurations);
	    TestAPI.as(TestAPI.SUPER_ADMIN).updateProductExecutionProfile(profile.id, realProfile);
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

	public ScenarioInitializer assignUserToProject(TestProject project, TestUser targetUser) {
		TestAPI.as(SUPER_ADMIN).assignUserToProject(targetUser, project);
		return this;
	}

}