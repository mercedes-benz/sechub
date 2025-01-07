// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;

public class CheckmarxExecutorConfigSuppportTest {

    private CheckmarxExecutorConfigSuppport supportToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetup setup;
    private List<ProductExecutorConfigSetupJobParameter> jobParameters;
    private SystemEnvironmentVariableSupport systemEnvironmentVariableSupport;
    private ProductExecutorContext context;

    @Before
    public void before() throws Exception {
        context = mock();
        systemEnvironmentVariableSupport = mock(SystemEnvironmentVariableSupport.class);
        config = mock(ProductExecutorConfig.class);
        when(context.getExecutorConfig()).thenReturn(config);
        setup = mock(ProductExecutorConfigSetup.class);

        jobParameters = new ArrayList<>();
        when(setup.getJobParameters()).thenReturn(jobParameters);
        when(config.getSetup()).thenReturn(setup);

    }

    @Test
    public void client_secret_returns_default_when_not_configured() {
        /* prepare */
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        assertEquals(CheckmarxConstants.DEFAULT_CLIENT_SECRET, supportToTest.getClientSecret());
    }

    @Test
    public void client_secret_returns_configured_value_when_parameter_available() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_CLIENT_SECRET, "new.secret"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        /* execute +test */
        assertEquals("new.secret", supportToTest.getClientSecret());
    }

    @Test
    public void client_secret_returns_an_empty_string_default_is_returned() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_CLIENT_SECRET, ""));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        /* execute +test */
        assertEquals(CheckmarxConstants.DEFAULT_CLIENT_SECRET, supportToTest.getClientSecret());
    }

    @Test
    public void engine_configuration_name_returns_default_when_not_configured() {
        /* prepare */
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        assertEquals(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME, supportToTest.getEngineConfigurationName());
    }

    @Test
    public void engine_configuration_name_returns_configured_value_when_parameter_available() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_ENGINE_CONFIGURATIONNAME, "test.engine"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        /* execute +test */
        assertEquals("test.engine", supportToTest.getEngineConfigurationName());
    }

    @Test
    public void engine_configuration_name_returns_an_empty_string_default_is_returned() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_ENGINE_CONFIGURATIONNAME, ""));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        /* execute +test */
        assertEquals(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME, supportToTest.getEngineConfigurationName());
    }

    @Test
    public void always_fullscan_enabled_true() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_FULLSCAN_ALWAYS, "true"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        /* test */
        assertEquals(true, supportToTest.isAlwaysFullScanEnabled());
    }

    @Test
    public void always_fullscan_enabled_false() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_FULLSCAN_ALWAYS, "false"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(context, systemEnvironmentVariableSupport);

        /* test */
        assertEquals(false, supportToTest.isAlwaysFullScanEnabled());
    }

}
