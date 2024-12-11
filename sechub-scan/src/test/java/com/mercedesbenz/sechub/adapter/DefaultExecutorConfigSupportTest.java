// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void before() throws Exception {
        config = mock(ProductExecutorConfig.class);
        context = mock();
        when(context.getExecutorConfig()).thenReturn(config);
        setup = mock(ProductExecutorConfigSetup.class);

        jobParameters = new ArrayList<>();
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key1", "value1"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key2", "2"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key3", "true"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key4", "false"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key5", "TRUE"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key6", null));

        when(config.getSetup()).thenReturn(setup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(setup.getCredentials()).thenReturn(credentialsInConfigSetup);

        when(setup.getJobParameters()).thenReturn(jobParameters);

        environmentVariableSupport = mock(SystemEnvironmentVariableSupport.class);
        supportToTest = new DefaultExecutorConfigSupport(context, environmentVariableSupport, null);
    }

    @Test
    public void support_returns_for_key_null_null_as_object() {
        assertEquals(null, supportToTest.getParameter(null));
    }

    @Test
    public void support_returns_for_key_null_fals_as_boolean() {
        assertEquals(false, supportToTest.getParameterBooleanValue(null));
    }

    @Test
    public void support_returns_for_key_null_n1_as_integer() {
        assertEquals(-1, supportToTest.getParameterIntValue(null));
    }

    @Test
    public void support_returns_for_key1_value1() {
        assertEquals("value1", supportToTest.getParameter("key1"));
    }

    @Test
    public void support_returns_for_key1_n1_when_fetched_as_int() {
        assertEquals(-1, supportToTest.getParameterIntValue("key1"));
    }

    @Test
    public void support_returns_for_key2_int_2() {
        assertEquals(2, supportToTest.getParameterIntValue("key2"));
    }

    @Test
    public void support_returns_for_key3_with_value_true_a_true() {
        assertEquals(true, supportToTest.getParameterBooleanValue("key3"));
    }

    @Test
    public void support_returns_for_key3_with_value_true_a_n1_when_fetched_as_integer() {
        assertEquals(-1, supportToTest.getParameterIntValue("key3"));
    }

    @Test
    public void support_returns_for_key5_with_value_TRUE_a_true() {
        assertEquals(true, supportToTest.getParameterBooleanValue("key5"));
    }

    @Test
    public void support_returns_for_key6_with_value_null_false() {
        assertEquals(false, supportToTest.getParameterBooleanValue("key6"));
    }

    @Test
    public void support_returns_for_key4_false() {
        assertEquals(false, supportToTest.getParameterBooleanValue("key4"));
    }

    @Test
    public void support_returns_for_an_unknown_key_false() {
        assertEquals(false, supportToTest.getParameterBooleanValue("i-am-unknown..."));
    }

    @Test
    public void support_returns_for_an_unknown_key_n1_when_fetched_as_integer() {
        assertEquals(-1, supportToTest.getParameterIntValue("i-am-unknown..."));
    }

    @Test
    public void support_returns_for_key1_false_when_forced_as_boolean() {
        assertEquals(false, supportToTest.getParameterBooleanValue("key1"));
    }

    @Test
    public void env_variable_support_handles_user_and_password() {

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
