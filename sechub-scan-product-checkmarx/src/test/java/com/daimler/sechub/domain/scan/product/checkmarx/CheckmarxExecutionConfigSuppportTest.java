// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.SystemEnvironment;

public class CheckmarxExecutionConfigSuppportTest {

    private CheckmarxExecutorConfigSuppport supportToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetup setup;
    private ProductExecutorConfigSetupCredentials credentialsInConfigSetup;
    private SystemEnvironment systemEnvironment;
    private List<ProductExecutorConfigSetupJobParameter> jobParameters;

    @Before
    public void before() throws Exception {
        config = mock(ProductExecutorConfig.class);
        setup = mock(ProductExecutorConfigSetup.class);

        jobParameters = new ArrayList<>();

        when(config.getSetup()).thenReturn(setup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(setup.getCredentials()).thenReturn(credentialsInConfigSetup);

        when(setup.getJobParameters()).thenReturn(jobParameters);

        systemEnvironment = mock(SystemEnvironment.class);
    }

    @Test
    public void always_fullscan_enabled_true() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_FULLSCAN_ALWAYS, "true"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(config, systemEnvironment);

        /* test */
        assertEquals(true, supportToTest.isAlwaysFullScanEnabled());
    }

    @Test
    public void always_fullscan_enabled_false() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_FULLSCAN_ALWAYS, "false"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(config, systemEnvironment);

        /* test */
        assertEquals(false, supportToTest.isAlwaysFullScanEnabled());
    }

}
