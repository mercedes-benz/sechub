package com.daimler.sechub.domain.scan.product.pds;

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

public class PDSExecutionConfigSuppportTest {

    private PDSExecutionConfigSuppport supportToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetup setup;
    private ProductExecutorConfigSetupCredentials credentialsInConfigSetup;
    private SystemEnvironment systemEnvironment;
    private List<ProductExecutorConfigSetupJobParameter> jobParameters;
    
    @Before
    public void before() throws Exception {
        config = mock(ProductExecutorConfig.class);
        setup = mock(ProductExecutorConfigSetup.class);
        
        jobParameters=new ArrayList<>();
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeys.PDS_PRODUCT_IDENTIFIER.getKey().getId(),"something"));
        
        when(config.getSetup()).thenReturn(setup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(setup.getCredentials()).thenReturn(credentialsInConfigSetup);
        
        when(setup.getJobParameters()).thenReturn(jobParameters);
        
        systemEnvironment = mock(SystemEnvironment.class);
        supportToTest=PDSExecutionConfigSuppport.createSupportAndAssertConfigValid(config,systemEnvironment);
    }

    @Test
    public void direct_credentials_in_config_setup_are_returned_directly() {
        /* prepare */
        credentialsInConfigSetup.setUser("user1");
        credentialsInConfigSetup.setPassword("pwd1");
        
        /* execute + test */
        assertEquals("user1", supportToTest.getUser());
        assertEquals("pwd1", supportToTest.getPasswordOrAPIToken());
    }
    
    @Test
    public void env_marked_credentials_in_config_setup_are_returned_evaluated() {
        
        /* prepare */
        String tempUserName = "testuser"+System.currentTimeMillis();
        String tempPwdFake= "pwd"+System.currentTimeMillis();
        
        when(systemEnvironment.getEnv("INTTEST_SECHUB_PDS_USERNAME")).thenReturn(tempUserName);
        when(systemEnvironment.getEnv("INTTEST_SECHUB_PDS_PWD")).thenReturn(tempPwdFake);
        
        credentialsInConfigSetup.setUser("env:INTTEST_SECHUB_PDS_USERNAME");
        credentialsInConfigSetup.setPassword("env:INTTEST_SECHUB_PDS_PWD");
        
        /* execute + test */
        assertEquals(tempUserName, supportToTest.getUser());
        assertEquals(tempPwdFake, supportToTest.getPasswordOrAPIToken());
    }

}
