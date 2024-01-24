// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.TestCanaryException;

class UserEmailAddressUpdateServiceTest {

    private static final String KNOWN_USER1 = "knownUser1";
    private UserEmailAddressUpdateService serviceToTest;
    private UserInputAssertion assertion;
    private AuditLogService auditLogService;
    private DomainMessageService eventBusService;
    private UserRepository userRepository;
    private LogSanitizer logSanitizer;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new UserEmailAddressUpdateService();

        assertion = mock(UserInputAssertion.class);
        auditLogService = mock(AuditLogService.class);
        eventBusService = mock(DomainMessageService.class);
        userRepository = mock(UserRepository.class);
        logSanitizer = mock(LogSanitizer.class);

        serviceToTest.assertion = assertion;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.logSanitizer = logSanitizer;
        serviceToTest.eventBusService = eventBusService;
        serviceToTest.userRepository = userRepository;
    }

    @Test
    void audit_log_is_called_with_sanitized_user_id_when_user_was_found() {
        /* prepare */
        User knownUser1 = createKnownUser1();
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);
        when(logSanitizer.sanitize(knownUser1.getName(), 30)).thenReturn("sanitized-userid");

        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, "new.user1@example.com");

        /* test */
        verify(auditLogService).log(any(String.class), eq("sanitized-userid"));
    }

    @Test
    void audit_log_is_NOT_called_when_user_not_found() {
        /* prepare */
        when(userRepository.findOrFailUser("notfound")).thenThrow(TestCanaryException.class);

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress("notfound", "new.user1@example.com"));

        /* test */
        verify(auditLogService, never()).log(any(String.class), any());
    }

    @Test
    void asserts_email_address_parameter() {
        /* prepare - just to have no NPE while testing with mock data */
        User knownUser1 = createKnownUser1();
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, "new.user1@example.com");

        /* test */
        verify(assertion).assertIsValidEmailAddress("new.user1@example.com");
    }

    @Test
    void asserts_user_id_parameter() {
        /* prepare - just to have no NPE while testing with mock data */
        User knownUser1 = createKnownUser1();
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, "new.user1@example.com");

        /* test */
        verify(assertion).assertIsValidUserId(KNOWN_USER1);
    }

    @Test
    void asserts_user_id_parameter_before_user_is_fetched_from_db() {
        /* prepare - just to have no NPE while testing with mock data */
        when(userRepository.findOrFailUser("notfound")).thenThrow(NotFoundException.class);
        doThrow(TestCanaryException.class).when(assertion).assertIsValidUserId(any());

        /* execute + test */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress("novalid", "former.user1@example.com"));
    }

    @Test
    void asserts_email_parameter_before_user_is_fetched_from_db() {
        /* prepare - just to have no NPE while testing with mock data */
        when(userRepository.findOrFailUser("notfound")).thenThrow(NotFoundException.class);
        doThrow(TestCanaryException.class).when(assertion).assertIsValidEmailAddress(any());

        /* execute + test */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress("notfound", "not-a-valid-email-address"));
    }

    @Test
    void when_assertions_do_not_handle_null_userid_user_repository_would_be_called_without_npe() {
        /* prepare */
        when(userRepository.findOrFailUser(null)).thenThrow(TestCanaryException.class);

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress(null, "something"));
    }

    @Test
    void when_assertions_do_not_handle_null_email_user_repository_would_be_called_without_npe() {
        /* prepare */
        when(userRepository.findOrFailUser("somebody")).thenThrow(TestCanaryException.class);

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress("somebody", null));
    }

    @Test
    void throws_not_acceptable_when_same_email_address_as_before() {
        /* prepare */
        User knownUser1 = createKnownUser1();
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        /* execute + test (exception ) */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.updateUserEmailAddress(KNOWN_USER1, "former.user1@example.com"));
    }

    @Test
    void throws_exception_when_user_not_found() {
        /* prepare */
        when(userRepository.findOrFailUser(any())).thenThrow(NotFoundException.class);

        /* execute + test (exception ) */
        assertThrows(NotFoundException.class, () -> serviceToTest.updateUserEmailAddress("notfound", "new.user1@example.com"));
    }

    @Test
    void saves_user_when_parameters_are_valid() {
        /* prepare */
        User knownUser1 = createKnownUser1();
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, "new.user1@example.com");

        /* test */
        // check the user object has new mail address when saved:
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("new.user1@example.com", userCaptor.getValue().getEmailAddress());
    }

    @Test
    void sends_event_with_user_data_when_parameters_are_valid() {
        /* prepare */
        User knownUser1 = createKnownUser1();
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, "new.user1@example.com");

        /* test */
        // check event is sent with expected data
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBusService).sendAsynchron(messageCaptor.capture());
        UserMessage userMessage = messageCaptor.getValue().get(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA);
        assertNotNull(userMessage);

        assertEquals(KNOWN_USER1, userMessage.getUserId());
        assertEquals("new.user1@example.com", userMessage.getEmailAddress());
        assertEquals("former.user1@example.com", userMessage.getFormerEmailAddress());
    }

    private User createKnownUser1() {
        User user = new User();
        user.name = KNOWN_USER1;
        user.emailAddress = "former.user1@example.com";
        return user;
    }

}
