// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import static com.daimler.sechub.integrationtest.internal.RetryAssertionErrorRunner.*;

public class AssertExecutionProfile {

    public static AssertExecutionProfile assertProfile(String profileId) {
        TestExecutionProfile config = fetchConfig(profileId);
        assertNotNull(config);
        return new AssertExecutionProfile(config);
    }

    public static void assertProfileDoesNotExist(String profileId) {
        try {
            fetchConfig(profileId);
            fail("should not be able to fetch exection profile:" + profileId);
        } catch (HttpClientErrorException e) {
            assertEquals("Got error but status not as expected", HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

    private static TestExecutionProfile fetchConfig(String profileId) {
        return as(SUPER_ADMIN).fetchProductExecutionProfile(profileId);
    }

    private TestExecutionProfile config;

    public AssertExecutionProfile(TestExecutionProfile config) {
        this.config = config;
    }

    public AssertExecutionProfile hasDescritpion(String description) {
        assertEquals(description, config.description);
        return this;
    }

    public AssertExecutionProfile isEnabled() {
        return isEnabled(true);
    }

    public AssertExecutionProfile isNotEnabled() {
        return isEnabled(false);
    }

    private AssertExecutionProfile isEnabled(boolean expected) {
        assertEquals(expected, config.enabled);
        return this;
    }

    /**
     * Asserts this profile has EXACTLY given project id relations (count + ids)
     * 
     * @param projectIds
     * @return
     */
    public AssertExecutionProfile hasProjectIds(String... projectIds) {
        runWithRetries(5, () -> {
            for (String projectId : projectIds) {
                assertTrue("project did not contain project id:" + projectId, config.projectIds.contains(projectId));
            }
            if (projectIds.length != config.projectIds.size()) {
                fail("Expected project ids found, but there are additional ones:\nexpected:" + Arrays.asList(projectIds) + "\nfound:" + config.projectIds);
            }
        }, () -> {
            /* reload the config so we got current state */
            config = fetchConfig(config.id);
        });
        return this;
    }

    public AssertExecutionProfile hasConfigurations(UUID... uuids) {

        runWithRetries(5, () -> {
            for (UUID uuid : uuids) {
                TestExecutorConfig found = null;
                for (TestExecutorConfig config : config.configurations) {
                    if (config.uuid.equals(uuid)) {
                        found = config;
                        break;
                    }
                }
                assertNotNull("project did not contain configuration with uuid :" + uuid, found);
            }
            assertEquals("Expected configurations found, but there are additional ones", uuids.length, config.configurations.size());
        }, () -> {
            /* reload the config so we got current state */
            config = fetchConfig(config.id);
        });
        return this;

    }

    public AssertExecutionProfile hasNoProjectIds() {
        return hasProjectIds();
    }

    public AssertExecutionProfile hasNoConfigurations() {
        return hasConfigurations();
    }

}
