package com.mercedesbenz.sechub.domain.schedule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class ScheduleSecHubConfigurationRuntimeValidationTest {

    @Mock
    DomainMessageService domainMessageService;
    private ScheduleSecHubConfigurationRuntimeValidation validationToTest;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        validationToTest = new ScheduleSecHubConfigurationRuntimeValidation(domainMessageService);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void sends_event_with_unencrypted_sechub_config_and_throws_no_exception_when_result_contains_no_errors(int variant) {
        /* prepare */
        SecHubConfiguration configuration = new SecHubConfiguration();
        configuration.setApiVersion("0815-just-for-testing");
        SecHubMessagesList errorMessages = new SecHubMessagesList();
        switch (variant) {
        case 0:
            /* nothing added */
            break;
        case 1:
            errorMessages.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.INFO, "i am just an info - so I will not throw an exception"));
            break;
        case 2:
            errorMessages.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.WARNING, "i am jut a warning - so I will not throw an exception"));
            break;

        default:
            fail("Not implemented variant!");

        }

        DomainMessageSynchronousResult domainMessageResult = mock(DomainMessageSynchronousResult.class);
        when(domainMessageResult.getMessageId()).thenReturn(MessageID.RESULT_FULL_CONFIGURATION_VALIDATION);

        when(domainMessageResult.get(MessageDataKeys.ERROR_MESSAGES)).thenReturn(errorMessages);
        when(domainMessageService.sendSynchron(any())).thenReturn(domainMessageResult);

        /* execute */
        validationToTest.assertConfigurationValidAtRuntime(configuration);

        /* test */
        ArgumentCaptor<DomainMessage> captor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(domainMessageService).sendSynchron(captor.capture());

        DomainMessage domainMessageSent = captor.getValue();
        assertThat(domainMessageSent.getMessageId()).isEqualTo(MessageID.REQUEST_FULL_CONFIGURATION_VALIDATION);
        assertThat(domainMessageSent.get(MessageDataKeys.SECHUB_UNENCRYPTED_CONFIG).toFormattedJSON()).isEqualTo(configuration.toFormattedJSON());
    }

    @Test
    void when_received_result_contains_an_error_message_an_response_excpetion_with_422_and_error_message_is_thrown() {
        /* prepare */
        SecHubConfiguration configuration = new SecHubConfiguration();
        SecHubMessagesList errorMessages = new SecHubMessagesList();
        errorMessages.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.ERROR, "i am an error"));

        DomainMessageSynchronousResult result = mock(DomainMessageSynchronousResult.class);
        when(result.getMessageId()).thenReturn(MessageID.RESULT_FULL_CONFIGURATION_VALIDATION);

        when(result.get(MessageDataKeys.ERROR_MESSAGES)).thenReturn(errorMessages);
        when(domainMessageService.sendSynchron(any())).thenReturn(result);

        /* execute + test @formatter:off */
        assertThatThrownBy(() -> validationToTest.assertConfigurationValidAtRuntime(configuration)).
                isInstanceOf(ResponseStatusException.class).
                satisfies((s) -> ((ResponseStatusException) s).getReason().equals("i am an error")).
                satisfies((s) -> ((ResponseStatusException) s).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(422)));
        /*@formatter:on*/
    }

}
