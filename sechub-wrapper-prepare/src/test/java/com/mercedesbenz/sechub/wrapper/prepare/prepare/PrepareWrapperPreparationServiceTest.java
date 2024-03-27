package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.model.RemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.RemoteCredentialContainer;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperResultStatus;

class PrepareWrapperPreparationServiceTest {

    private PrepareWrapperPreparationService serviceToTest;

    PrepareWrapperEnvironment environment;

    PrepareWrapperContextFactory factory;

    PrepareWrapperResultStatus status;

    PrepareWrapperContext context;

    @BeforeEach
    void beforeEach() {

        environment = mock(PrepareWrapperEnvironment.class);
        factory = mock(PrepareWrapperContextFactory.class);
        status = new PrepareWrapperResultStatus();
        context = mock(PrepareWrapperContext.class);

        when(factory.create(environment)).thenReturn(context);

        serviceToTest = new PrepareWrapperPreparationService();
        serviceToTest.environment = environment;
        serviceToTest.factory = factory;
        serviceToTest.status = status;

    }

    @Test
    void when_no_remote_data_was_configured_return_preparation_success(){
        /* prepare */
        when(context.getSecHubConfiguration()).thenReturn(new SecHubConfigurationModel());
        Map<String, Pattern> map = new LinkedHashMap<>();
        when(context.getRemoteCredentialContainer()).thenReturn(new RemoteCredentialContainer(new RemoteCredentialConfiguration(), map));

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=ok", result.getProductResult());
    }

    @Test
    void when_context_was_not_filled_correctly_throws_exception() {
        /* execution + test */
        assertThrows(IllegalStateException.class, () -> serviceToTest.startPreparation());
    }

}