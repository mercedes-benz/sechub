// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;

class PrepareWrapperPreparationServiceTest {

    private PrepareWrapperPreparationService serviceToTest;
    private PrepareWrapperContext context;
    private PrepareWrapperModule testPrepareWrapperModule1;
    private PrepareWrapperModule testPrepareWrapperModule2;
    private static boolean NOT_ENABLED = false;
    private static boolean ENABLED = true;

    private static boolean NOT_RESPONSIBLE = false;
    private static boolean RESPONSIBLE = true;

    @BeforeEach
    void beforeEach() {
        context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        PrepareWrapperContextFactory contextFactory = mock(PrepareWrapperContextFactory.class);
        PrepareWrapperProxySupport proxySupport = mock(PrepareWrapperProxySupport.class);
        when(contextFactory.create(environment)).thenReturn(context);

        serviceToTest = new PrepareWrapperPreparationService();
        serviceToTest.environment = environment;
        serviceToTest.contextFactory = contextFactory;
        serviceToTest.modules = new ArrayList<>();
        serviceToTest.proxySupport = proxySupport;
    }

    @Test
    void when_no_remote_data_was_configured_and_no_modules_defined_return_preparation_success_with_warn_message() throws IOException {
        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation(); // no remote data configured, nor a module

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No Remote Configuration found.", result.getProductMessages().get(0).getText());
    }

    @Test
    void when_no_remote_data_was_configured_and_module_defined_and_enabled_and_responsible() throws IOException {
        /* prepare */
        installModule1(ENABLED, RESPONSIBLE); // but here we have no remote configuration!

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No Remote Configuration found.", result.getProductMessages().get(0).getText());

        // check no interaction with installed module in this case
        verify(testPrepareWrapperModule1, never()).isEnabled();
        verify(testPrepareWrapperModule1, never()).isResponsibleToPrepare(context);
        verify(testPrepareWrapperModule1, never()).prepare(context);
    }

    @Test
    void when_remote_data_was_configured_but_no_module_exists_preparation_failes_with_message() throws IOException {
        setupValidTestGitRemoteDataConfiguration();

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=FAILED", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No module was able to prepare the defined remote data.", result.getProductMessages().get(0).getText());
    }

    @Test
    void when_remote_data_was_configured_module_responsible_but_not_enabled_preparation_failes_with_message() throws IOException {
        /* prepare */
        installModule1(NOT_ENABLED, RESPONSIBLE);
        setupValidTestGitRemoteDataConfiguration();

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        verify(testPrepareWrapperModule1, never()).prepare(context); // prepare is never called at this module!
        assertEquals("SECHUB_PREPARE_RESULT;status=FAILED", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No module was able to prepare the defined remote data.", result.getProductMessages().get(0).getText());
    }

    @Test
    void when_remote_data_was_configured_but_module_enabled_but_not_responsible_preparation_failes_with_message() throws IOException {
        /* prepare */
        installModule1(ENABLED, NOT_RESPONSIBLE);
        setupValidTestGitRemoteDataConfiguration();

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        verify(testPrepareWrapperModule1, never()).prepare(context); // prepare is never called at this module!
        assertEquals("SECHUB_PREPARE_RESULT;status=FAILED", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No module was able to prepare the defined remote data.", result.getProductMessages().get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void data_available_two_modules_enabled_and_one_reponsible_calls_module_with_success(boolean firstModuleResponsible) throws IOException {
        /* prepare */
        /* prepare */
        installModule1(ENABLED, firstModuleResponsible);
        installModule2(ENABLED, !firstModuleResponsible);
        setupValidTestGitRemoteDataConfiguration();

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(0, result.getProductMessages().size());

        if (firstModuleResponsible) {
            verify(testPrepareWrapperModule1).prepare(context);
            verify(testPrepareWrapperModule2, never()).prepare(context);
        } else {
            verify(testPrepareWrapperModule1, never()).prepare(context);
            verify(testPrepareWrapperModule2).prepare(context);
        }
    }

    @Test
    void data_available_one_module_enabled_and_reponsible_calls_module_with_success() throws IOException {
        /* prepare */
        /* prepare */
        installModule1(ENABLED, RESPONSIBLE);
        setupValidTestGitRemoteDataConfiguration();

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(0, result.getProductMessages().size());

        verify(testPrepareWrapperModule1).prepare(context);
    }

    private void setupValidTestGitRemoteDataConfiguration() {
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example_location");
        remoteDataConfiguration.setType("git");
        when(context.getRemoteDataConfiguration()).thenReturn(remoteDataConfiguration);
    }

    private void installModule1(boolean moduleIsEnabled, boolean moduleIsResponsible) {
        testPrepareWrapperModule1 = mock(PrepareWrapperModule.class, "mockModule-1");
        serviceToTest.modules.add(testPrepareWrapperModule1);

        when(testPrepareWrapperModule1.isEnabled()).thenReturn(moduleIsEnabled);
        when(testPrepareWrapperModule1.isResponsibleToPrepare(context)).thenReturn(moduleIsResponsible);
    }

    private void installModule2(boolean moduleIsEnabled, boolean moduleIsResponsible) {
        testPrepareWrapperModule2 = mock(PrepareWrapperModule.class, "mockModule-2");
        serviceToTest.modules.add(testPrepareWrapperModule2);

        when(testPrepareWrapperModule2.isEnabled()).thenReturn(moduleIsEnabled);
        when(testPrepareWrapperModule2.isResponsibleToPrepare(context)).thenReturn(moduleIsResponsible);
    }
}