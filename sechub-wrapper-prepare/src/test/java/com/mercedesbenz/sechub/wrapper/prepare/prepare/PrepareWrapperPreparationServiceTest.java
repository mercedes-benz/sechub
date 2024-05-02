package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModuleGit;

class PrepareWrapperPreparationServiceTest {

    private PrepareWrapperPreparationService serviceToTest;

    PrepareWrapperEnvironment environment;

    PrepareWrapperContextFactory factory;

    PrepareWrapperContext context;

    @BeforeEach
    void beforeEach() {
        environment = mock(PrepareWrapperEnvironment.class);
        factory = mock(PrepareWrapperContextFactory.class);
        context = mock(PrepareWrapperContext.class);
        when(factory.create(environment)).thenReturn(context);

        serviceToTest = new PrepareWrapperPreparationService();
        serviceToTest.environment = environment;
        serviceToTest.factory = factory;
        serviceToTest.modules = new ArrayList<>();
    }

    @Test
    void when_no_remote_data_was_configured_return_preparation_success_with_warn_message() throws IOException {
        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No Remote Configuration found.", result.getProductMessages().get(0).getText());
    }

    @Test
    void when_remote_data_was_configured_but_no_module_executed_return_preparation_failed_with_message() throws IOException {
        /* prepare */
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example_location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        when(context.getRemoteDataConfigurationList()).thenReturn(remoteDataConfigurationList);

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=FAILED", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No module was able to prepare the defined remote data.", result.getProductMessages().get(0).getText());
    }

    @Test
    void when_remote_data_was_configured_and_git_module_added_return_preparation_success_without_message() throws IOException {
        /* prepare */
        PrepareWrapperModuleGit gitModule = mock(PrepareWrapperModuleGit.class);
        serviceToTest.modules.add(gitModule);

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example_location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        when(context.getRemoteDataConfigurationList()).thenReturn(remoteDataConfigurationList);

        when(gitModule.isAbleToPrepare(context)).thenReturn(true);

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(0, result.getProductMessages().size());

        verify(gitModule).prepare(context);
    }
}