// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.administration.OneTimeTokenGenerator;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class AnonymousUserRequestsNewApiTokenServiceTest {

    private static final String FAKE_ONE_TIME_TOKEN = "T1234567890";
    private AnonymousUserRequestsNewApiTokenService serviceToTest;
    private OneTimeTokenGenerator mockedOneTimeTokenGenerator;
    private SecHubEnvironment mockedEnvironment;
    private DomainMessageService mockedEventBusService;
    private UserRepository mockedUserRepository;
    private UserInputAssertion mockedUserAssertion;

    @Before
    public void before() {
        mockedOneTimeTokenGenerator = mock(OneTimeTokenGenerator.class);
        mockedEnvironment = mock(SecHubEnvironment.class);
        mockedEventBusService = mock(DomainMessageService.class);
        mockedUserRepository = mock(UserRepository.class);
        mockedUserAssertion = mock(UserInputAssertion.class);
        when(mockedOneTimeTokenGenerator.generateNewOneTimeToken()).thenReturn(FAKE_ONE_TIME_TOKEN);

        serviceToTest = new AnonymousUserRequestsNewApiTokenService();
        serviceToTest.oneTimeTokenGenerator = mockedOneTimeTokenGenerator;
        serviceToTest.environment = mockedEnvironment;
        serviceToTest.eventBusService = mockedEventBusService;
        serviceToTest.userRepository = mockedUserRepository;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mockedUserAssertion;
    }

    @Test
    public void service_uses_assertion_validate_mail() throws Exception {
        /* execute */
        serviceToTest.anonymousRequestToGetNewApiTokenForUserEmailAddress("user@test.com");

        /* test */
        verify(mockedUserAssertion).assertIsValidEmailAddress("user@test.com");
    }

    @Test
    public void when_emailaddress_not_found_no_exception_is_thrown() throws Exception {

        /* prepare */
        when(mockedUserRepository.findByEmailAddress("user@test.com")).thenReturn(Optional.empty());

        /* execute */
        serviceToTest.anonymousRequestToGetNewApiTokenForUserEmailAddress("user@test.com");

    }

    @Test
    public void when_emailaddress_found_a_new_async_event_is_sent_eventbus() throws Exception {

        User user = new User();
        user.emailAddress = "user@test.com";
        user.name = "testuser";

        /* prepare */
        when(mockedUserRepository.findByEmailAddress("user@test.com")).thenReturn(Optional.of(user));

        /* execute */
        serviceToTest.anonymousRequestToGetNewApiTokenForUserEmailAddress("user@test.com");

        /* test */
        ArgumentCaptor<DomainMessage> domainMessageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(mockedEventBusService).sendAsynchron(domainMessageCaptor.capture());

        DomainMessage messageSendByService = domainMessageCaptor.getValue();
        assertNotNull("no message send!", messageSendByService);
        UserMessage refreshApiKeyMessage = messageSendByService.get(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO);
        assertNotNull("no refersh api key data inside message!", refreshApiKeyMessage);
        // check event contains expected data
        assertNull(refreshApiKeyMessage.getUserId()); // user id not inside
        assertEquals("user@test.com", refreshApiKeyMessage.getEmailAddress());

    }

    @Test
    public void when_emailaddress_found__onetimetoken_created_and_persisted() throws Exception {

        User user = new User();
        user.emailAddress = "user@test.com";
        user.name = "testuser";

        /* prepare */
        when(mockedUserRepository.findByEmailAddress("user@test.com")).thenReturn(Optional.of(user));

        /* execute */
        serviceToTest.anonymousRequestToGetNewApiTokenForUserEmailAddress("user@test.com");

        /* test */
        assertEquals(FAKE_ONE_TIME_TOKEN, user.oneTimeToken);
        assertNotNull(user.oneTimeTokenDate);

        verify(mockedUserRepository).save(user);
    }

}
