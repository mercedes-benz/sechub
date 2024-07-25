// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.integrationtest.api.AbstractTestExecutable;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;

public class ScenarioInitializer {

    private static final int DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION = 3;
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioInitializer.class);
    private static boolean mappingCheckDone;

    public ScenarioInitializer createProject(TestProject project, TestUser owner) {
        TestAPI.as(TestAPI.SUPER_ADMIN).createProject(project, owner.getUserId());
        return this;
    }

    public ScenarioInitializer addProjectIdsToDefaultExecutionProfile(DefaultTestExecutionProfile profile, TestProject... projects) {
        ensureDefaultExecutionProfile(profile);
        TestAPI.as(TestAPI.SUPER_ADMIN).addProjectsToProfile(profile.id, projects);
        return this;
    }

    private ScenarioInitializer ensureDefaultExecutionProfile(DefaultTestExecutionProfile profile) {
        ensureDefaultMappingsAvailableInEveryProfile();
        if (TestAPI.canReloadExecutionProfileData(profile)) {
            return this;
        }
        Set<TestExecutorConfig> realConfigurations = new LinkedHashSet<>();

        /*
         * we iterate over initial list, where all defaults are inside - but UUID is
         * null...
         */
        for (TestExecutorConfig config : profile.initialConfigurationsWithoutUUID) {
            UUID uuid = TestAPI.as(SUPER_ADMIN).createProductExecutorConfig(config);
            config.uuid = uuid; // set UUID from service to this so available
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

    private void ensureDefaultMappingsAvailableInEveryProfile() {
        if (mappingCheckDone) {
            return;
        }
        ensureMapping1();
        mappingCheckDone = true;
    }

    private void ensureMapping1() {
        MappingData project1Mapping = TestAPI.fetchMappingDataDirectlyOrNull(IntegrationTestExampleConstants.MAPPING_ID_1_REPLACE_ANY_PROJECT1);
        if (project1Mapping != null) {
            return;
        }
        MappingEntry entry = new MappingEntry(IntegrationTestExampleConstants.MAPPING_1_PATTERN_ANY_PROJECT1,
                IntegrationTestExampleConstants.MAPPING_1_REPLACEMENT_FOR_PROJECT1, IntegrationTestExampleConstants.MAPPING_1_COMMENT);
        TestAPI.changeScanMappingDirectly(IntegrationTestExampleConstants.MAPPING_ID_1_REPLACE_ANY_PROJECT1, entry);
    }

    public static void main(String[] args) {
        MappingData project1Mapping = new MappingData();

        MappingEntry entry = new MappingEntry(IntegrationTestExampleConstants.MAPPING_1_PATTERN_ANY_PROJECT1,
                IntegrationTestExampleConstants.MAPPING_1_REPLACEMENT_FOR_PROJECT1, IntegrationTestExampleConstants.MAPPING_1_COMMENT);
        project1Mapping.getEntries().add(entry);
        System.out.println(project1Mapping.toJSON());
    }

    /**
     * Creates an user, updates the token
     *
     * @param user
     * @return
     */
    public ScenarioInitializer createUser(TestUser user) {

        LOG.info("create user:{}", user.getUserId());
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
        return waitUntilUserCanLogin(user, DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
    }

    public ScenarioInitializer waitUntilUserCanLogin(TestUser user, int secondsBeforeTimeOut) {
        TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(user, secondsBeforeTimeOut, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                assertUser(user).canLogin();
                return true;
            }
        });
        return this;
    }

    public ScenarioInitializer waitUntilUserExists(TestUser user) {
        return waitUntilUserExists(user, DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
    }

    public ScenarioInitializer waitUntilUserExists(TestUser user, int secondsBeforeTimeOut) {
        TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, secondsBeforeTimeOut, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                assertUser(user).doesExist();
                return true;
            }
        });
        return this;
    }

    public ScenarioInitializer waitUntilProjectExists(TestProject project) {
        return waitUntilProjectExists(project, DEFAULT_TIME_TO_WAIT_FOR_RESOURCE_CREATION);
    }

    public ScenarioInitializer waitUntilProjectExists(TestProject project, int secondsBeforeTimeOut) {
        TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, secondsBeforeTimeOut, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
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