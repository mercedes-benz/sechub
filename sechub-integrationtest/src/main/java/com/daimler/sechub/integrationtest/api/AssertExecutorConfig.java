// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class AssertExecutorConfig {

    public static AssertExecutorConfig assertConfig(UUID uuid) {
        TestExecutorConfig config = fetchConfig(uuid);
        assertNotNull(config);
        return new AssertExecutorConfig(config);
    }

    public static void assertConfigDoesNotExist(UUID uuid) {
        try {
            fetchConfig(uuid);
            fail("should not be able to fetch exector conifg!");
        }catch(HttpClientErrorException e) {
            assertEquals("Got error but status not as expected", HttpStatus.NOT_FOUND,e.getStatusCode());
        }
    }
    
    private static TestExecutorConfig fetchConfig(UUID uuid) {
        return as(SUPER_ADMIN).fetchProductExecutorConfig(uuid);
    }
    

    private TestExecutorConfig config;

    public AssertExecutorConfig(TestExecutorConfig config) {
        this.config = config;
    }

    public AssertExecutorConfig hasName(String name) {
        assertEquals(name, config.name);
        return this;
    }

    public AssertExecutorConfig hasProductIdentfiier(String id) {
        assertEquals(id, config.productIdentifier);
        return this;
    }

    public AssertExecutorConfig hasBaseURL(String url) {
        assertEquals(url, config.setup.baseURL);
        return this;
    }

    public AssertExecutorConfig hasExecutorVersion(int version) {
        assertEquals(version, config.executorVersion);
        return this;
    }

    public AssertExecutorConfig isEnabled() {
        return isEnabled(true);
    }
    
    public AssertExecutorConfig isNotEnabled() {
        return isEnabled(false);
    }
    
    private AssertExecutorConfig isEnabled(boolean enabled) {
        assertEquals(enabled, config.enabled);
        return this;
    }

    public AssertExecutorConfig hasCredentials(String user, String password) {
        assertEquals(user, config.setup.credentials.user);
        assertEquals(password, config.setup.credentials.password);
        return this;
    }

    public AssertExecutorConfig hasJobParameter(String key, String value) {
        TestExecutorSetupJobParam found = null;
        for (TestExecutorSetupJobParam param : config.setup.jobParameters) {
            if (key.equals(param.key)) {
                found = param;
                break;
            }
        }
        assertNotNull("Did not found job parameter with key:" + key, found);
        assertEquals("Value of job paramater not as expected!", value, found.value);
        return this;
    }

    public AssertExecutorConfig hasJobParameters(int expected) {
        assertEquals("Not expecte amount of job parameters!", expected,config.setup.jobParameters.size());
        return this;
    }

}
