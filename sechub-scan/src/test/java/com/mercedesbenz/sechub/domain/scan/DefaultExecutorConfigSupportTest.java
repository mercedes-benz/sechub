// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;

public class DefaultExecutorConfigSupportTest {
    private DefaultExecutorConfigSupport supportToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetup setup;
    private ProductExecutorConfigSetupCredentials credentialsInConfigSetup;
    private SystemEnvironmentVariableSupport environmentVariableSupport;
    private List<ProductExecutorConfigSetupJobParameter> jobParameters;
    private ProductExecutorContext context;

    @BeforeEach
    void before() throws Exception {
        config = mock(ProductExecutorConfig.class);
        context = mock();
        when(context.getExecutorConfig()).thenReturn(config);
        setup = mock(ProductExecutorConfigSetup.class);

        jobParameters = new ArrayList<>();
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key1", "value1"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key2", "2"));

        when(config.getSetup()).thenReturn(setup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(setup.getCredentials()).thenReturn(credentialsInConfigSetup);

        when(setup.getJobParameters()).thenReturn(jobParameters);

        environmentVariableSupport = mock(SystemEnvironmentVariableSupport.class);
        supportToTest = new DefaultExecutorConfigSupport(context, environmentVariableSupport, null);
    }

    @Test
    void jobParameterprovider_is_created_and_provides_expected_keys() {
        assertThat(supportToTest.getJobParameterProvider()).isNotNull();
        assertThat(supportToTest.getJobParameterProvider().getKeys()).contains("key1", "key2");
        assertThat(supportToTest.getJobParameterProvider().get("key1")).isEqualTo("value1");
        assertThat(supportToTest.getJobParameterProvider().get("key2")).isEqualTo("2");
    }

    @Test
    void env_variable_support_handles_user_and_password() {

        /* prepare */
        String environmentTestUserName = "testuser" + System.currentTimeMillis();
        String environmentTestPwd = "pwd" + System.currentTimeMillis();

        when(environmentVariableSupport.getValueOrVariableContent("a")).thenReturn(environmentTestUserName);
        when(environmentVariableSupport.getValueOrVariableContent("b")).thenReturn(environmentTestPwd);

        credentialsInConfigSetup.setUser("a");
        credentialsInConfigSetup.setPassword("b");

        /* execute + test */
        assertEquals(environmentTestUserName, supportToTest.getUser());
        assertEquals(environmentTestPwd, supportToTest.getPasswordOrAPIToken());
    }

}
