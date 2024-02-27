// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class SignupCreateServiceTest {

    private AnonymousSignupCreateService serviceToTest;
    private DomainMessageService mockedEventBusService;

    @Before
    public void before() {
        mockedEventBusService = mock(DomainMessageService.class);

        serviceToTest = new AnonymousSignupCreateService();
        serviceToTest.eventBusService = mockedEventBusService;
        serviceToTest.userRepository = mock(UserRepository.class);
        serviceToTest.userSelfRegistrationRepository = mock(SignupRepository.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);

    }

    @Test
    public void a_created_signup_sends_event_containing_userid_and_email() {
        /* prepare */
        SignupJsonInput userSelfRegistrationInput = mock(SignupJsonInput.class);
        when(userSelfRegistrationInput.getUserId()).thenReturn("adam42");
        when(userSelfRegistrationInput.getEmailAddress()).thenReturn("adam42@example.org");

        /* execute */
        serviceToTest.register(userSelfRegistrationInput);

        /* test */
        ArgumentCaptor<DomainMessage> domainMessageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(mockedEventBusService).sendAsynchron(domainMessageCaptor.capture());

        DomainMessage messageSendByService = domainMessageCaptor.getValue();
        assertNotNull("no message send!", messageSendByService);
        UserMessage signupDataInMessage = messageSendByService.get(MessageDataKeys.USER_SIGNUP_DATA);
        assertNotNull("no signup data inside message!", signupDataInMessage);
        // check event contains expected data
        assertEquals("adam42", signupDataInMessage.getUserId());
        assertEquals("adam42@example.org", signupDataInMessage.getEmailAddress());
    }

}
