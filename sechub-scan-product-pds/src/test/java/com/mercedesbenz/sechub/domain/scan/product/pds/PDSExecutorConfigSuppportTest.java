// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;

public class PDSExecutorConfigSuppportTest {

    private static final String CONFIGURED_PDS_PRODUCT_IDENTIFIER = "a_string";
    private PDSExecutorConfigSuppport supportToTest;
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
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_PRODUCTIDENTIFIER.getKey().getId(),
                CONFIGURED_PDS_PRODUCT_IDENTIFIER));
        jobParameters
                .add(new ProductExecutorConfigSetupJobParameter(SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTERNET.getKey().getId(), "true"));
        jobParameters.add(
                new ProductExecutorConfigSetupJobParameter(SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTRANET.getKey().getId(), "false"));

        when(config.getSetup()).thenReturn(setup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(setup.getCredentials()).thenReturn(credentialsInConfigSetup);

        when(setup.getJobParameters()).thenReturn(jobParameters);

        systemEnvironment = mock(SystemEnvironment.class);
        supportToTest = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(config, systemEnvironment);
    }

    @Test
    public void getPDSProductIdentifier_returns_configured_value() {
        assertEquals(CONFIGURED_PDS_PRODUCT_IDENTIFIER, supportToTest.getPDSProductIdentifier());
    }

    @Test
    public void isTargetTypeForbidden_returns_true_for_target_type_requested_is_internet_when_internet_is_forbidden_in_configuration() {
        assertEquals(true, supportToTest.isTargetTypeForbidden(NetworkTargetType.INTERNET));
    }

    @Test
    public void isTargetTypeForbidden_returns_false_for_target_type_requested_is_intranet_when_internet_is_forbidden_in_configuration() {
        assertEquals(false, supportToTest.isTargetTypeForbidden(NetworkTargetType.INTRANET));
    }

}
