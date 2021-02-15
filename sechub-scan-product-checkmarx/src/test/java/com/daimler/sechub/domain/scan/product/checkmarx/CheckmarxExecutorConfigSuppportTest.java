package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.checkmarx.CheckmarxConstants;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.SystemEnvironment;

public class CheckmarxExecutorConfigSuppportTest {



private CheckmarxExecutorConfigSuppport supportToTest;
private ProductExecutorConfig config;
private ProductExecutorConfigSetup setup;
private List<ProductExecutorConfigSetupJobParameter> jobParameters;
private SystemEnvironment systemEnvironment;

@Before
public void before() throws Exception {
    
    systemEnvironment = mock(SystemEnvironment.class);
    config = mock(ProductExecutorConfig.class);
    setup = mock(ProductExecutorConfigSetup.class);
    
    jobParameters=new ArrayList<>();
    when(setup.getJobParameters()).thenReturn(jobParameters);
    when(config.getSetup()).thenReturn(setup);
    
}

    @Test
    public void engine_configuration_name_returns_default_when_not_configured() {
        /* prepare */
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(config,systemEnvironment);
        
        assertEquals(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME, supportToTest.getEngineConfigurationName());
    }
    
    @Test
    public void engine_configuration_name_returns_configured_value_when_parameter_available() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_ENGINE_CONFIGURATIONNAME, "test.engine.2020"));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(config,systemEnvironment);
        
        /* execute +test */
        assertEquals("test.engine.2020", supportToTest.getEngineConfigurationName());
    }
    
    
    @Test
    public void engine_configuration_name_returns_an_empty_string_default_is_returned() {
        /* prepare */
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_ENGINE_CONFIGURATIONNAME, ""));
        supportToTest = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(config,systemEnvironment);
        
        /* execute +test */
        assertEquals(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME, supportToTest.getEngineConfigurationName());
    }

}
