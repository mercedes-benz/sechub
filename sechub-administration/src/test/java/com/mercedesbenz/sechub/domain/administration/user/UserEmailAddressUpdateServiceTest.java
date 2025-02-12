// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.TestCanaryException;

class UserEmailAddressUpdateServiceTest {

    private static final String KNOWN_USER1 = "knownUser1";
    public static final String FORMER_USER_1_EXAMPLE_COM = "former.user1@example.com";
    public static final String NEW_MAIL_USER1_EXAMPLE_COM = "new.user1@example.com";
    private final User knownUser1 = createKnownUser1();
    private UserEmailAddressUpdateService serviceToTest;
    private DomainMessageService eventBusService;
    private UserRepository userRepository;
    private AuditLogService auditLogService;
    private LogSanitizer logSanitizer;
    private UserInputAssertion assertion;
    private UserContextService userContextService;
    private UserEmailChangeTokenService userEmailChangeTokenService;
    private SecHubEnvironment environment;

    @BeforeEach
    void beforeEach() {
        assertion = mock(UserInputAssertion.class);
        auditLogService = mock(AuditLogService.class);
        eventBusService = mock(DomainMessageService.class);
        userRepository = mock(UserRepository.class);
        logSanitizer = mock(LogSanitizer.class);
        userContextService = mock(UserContextService.class);
        userEmailChangeTokenService = mock(UserEmailChangeTokenService.class);
        environment = mock(SecHubEnvironment.class);

        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        serviceToTest = new UserEmailAddressUpdateService(eventBusService, userRepository, auditLogService, logSanitizer, assertion, userContextService,
                userEmailChangeTokenService, environment);
    }

    @Test
    void audit_log_is_called_with_sanitized_user_id_when_user_was_found() {
        /* prepare */
        when(logSanitizer.sanitize(knownUser1.getName(), 30)).thenReturn("sanitized-userid");

        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, NEW_MAIL_USER1_EXAMPLE_COM);

        /* test */
        verify(auditLogService).log(any(String.class), eq("sanitized-userid"));
    }

    @Test
    void audit_log_is_NOT_called_when_user_not_found() {
        /* prepare */
        when(userRepository.findOrFailUser("notfound")).thenThrow(TestCanaryException.class);

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress("notfound", NEW_MAIL_USER1_EXAMPLE_COM));

        /* test */
        verify(auditLogService, never()).log(any(String.class), any());
    }

    @Test
    void asserts_email_address_parameter() {
        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, NEW_MAIL_USER1_EXAMPLE_COM);

        /* test */
        verify(assertion).assertIsValidEmailAddress(NEW_MAIL_USER1_EXAMPLE_COM);
    }

    @Test
    void asserts_user_id_parameter() {
        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, NEW_MAIL_USER1_EXAMPLE_COM);

        /* test */
        verify(assertion).assertIsValidUserId(KNOWN_USER1);
    }

    @Test
    void asserts_user_id_parameter_before_user_is_fetched_from_db() {
        /* prepare - just to have no NPE while testing with mock data */
        when(userRepository.findOrFailUser("notfound")).thenThrow(NotFoundException.class);
        doThrow(TestCanaryException.class).when(assertion).assertIsValidUserId(any());

        /* execute + test */
        assertThrows(TestCanaryException.class, () -> serviceToTest.updateUserEmailAddress("novalid", FORMER_USER_1_EXAMPLE_COM));
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
    void throws_bad_request_when_same_email_address_as_before() {
        /* execute + test (exception ) */
        assertThrows(BadRequestException.class, () -> serviceToTest.updateUserEmailAddress(KNOWN_USER1, FORMER_USER_1_EXAMPLE_COM));
    }

    @Test
    void throws_exception_when_user_not_found() {
        /* prepare */
        when(userRepository.findOrFailUser(any())).thenThrow(NotFoundException.class);

        /* execute + test (exception ) */
        assertThrows(NotFoundException.class, () -> serviceToTest.updateUserEmailAddress("notfound", NEW_MAIL_USER1_EXAMPLE_COM));
    }

    @Test
    void saves_user_when_parameters_are_valid() {
        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, NEW_MAIL_USER1_EXAMPLE_COM);

        /* test */
        // check the user object has new mail address when saved:
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(NEW_MAIL_USER1_EXAMPLE_COM, userCaptor.getValue().getEmailAddress());
    }

    @Test
    void sends_event_with_user_data_when_parameters_are_valid() {
        /* execute */
        serviceToTest.updateUserEmailAddress(KNOWN_USER1, NEW_MAIL_USER1_EXAMPLE_COM);

        /* test */
        // check event is sent with expected data
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBusService).sendAsynchron(messageCaptor.capture());
        UserMessage userMessage = messageCaptor.getValue().get(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA);
        assertNotNull(userMessage);

        assertEquals(KNOWN_USER1, userMessage.getUserId());
        assertEquals(NEW_MAIL_USER1_EXAMPLE_COM, userMessage.getEmailAddress());
        assertEquals(FORMER_USER_1_EXAMPLE_COM, userMessage.getFormerEmailAddress());
    }

    @Test
    void request_update_email_address_with_same_email_throws_BadRequestException(){
        /* prepare */
        when(userContextService.getUserId()).thenReturn(KNOWN_USER1);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userRequestUpdateMailAddress(FORMER_USER_1_EXAMPLE_COM))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User has already this email address");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", " ", "  " })
    void request_update_email_throws_BadRequestException_when_email_is_null(String email){
        /* prepare */
        when(userContextService.getUserId()).thenReturn(KNOWN_USER1);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userRequestUpdateMailAddress(email))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email must not be empty");
    }

    @Test
    void request_update_email_address_with_different_email_sends_event() {
        /* prepare */
        when(userContextService.getUserId()).thenReturn(KNOWN_USER1);
        when(userEmailChangeTokenService.generateToken(any(), any()))
                .thenReturn("token");
        /* execute */
        serviceToTest.userRequestUpdateMailAddress(NEW_MAIL_USER1_EXAMPLE_COM);

        /* test */
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBusService).sendAsynchron(messageCaptor.capture());

        UserMessage userMessage = messageCaptor.getValue().get(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA);
        assertThat(userMessage).isNotNull();
        assertThat("You have requested to change your SecHub email address").isEqualTo(userMessage.getSubject());
        assertThat(KNOWN_USER1).isEqualTo(userMessage.getUserId());
        assertThat(NEW_MAIL_USER1_EXAMPLE_COM).isEqualTo(userMessage.getEmailAddress());
        assertThat (FORMER_USER_1_EXAMPLE_COM).isEqualTo(userMessage.getFormerEmailAddress());
        assertThat(userMessage.getLinkWithOneTimeToken()).isNotNull();
    }

    @Test
    void user_verifies_user_email_address_throws_BadRequestException_when_user_has_already_this_email_address() {
        /* prepare */
        when(userEmailChangeTokenService.extractUserInfoFromToken(any())).thenReturn(new UserEmailChangeRecord(KNOWN_USER1, FORMER_USER_1_EXAMPLE_COM));
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userVerifiesUserEmailAddress("token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User has already this email address");
    }

    @Test
    void user_verifies_user_email_address_sends_event() {
        /* prepare */
        when(userEmailChangeTokenService.extractUserInfoFromToken(any())).thenReturn(new UserEmailChangeRecord(KNOWN_USER1, NEW_MAIL_USER1_EXAMPLE_COM));
        when(userRepository.findOrFailUser(KNOWN_USER1)).thenReturn(knownUser1);
        assertThat(knownUser1.getEmailAddress()).isEqualTo(FORMER_USER_1_EXAMPLE_COM);

        /* execute */
        serviceToTest.userVerifiesUserEmailAddress("token");

        /* test */
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBusService).sendAsynchron(messageCaptor.capture());

        UserMessage userMessage = messageCaptor.getValue().get(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA);
        assertThat(userMessage).isNotNull();
        assertThat("Your SecHub email address has been changed").isEqualTo(userMessage.getSubject());
        assertThat(KNOWN_USER1).isEqualTo(userMessage.getUserId());
        assertThat(NEW_MAIL_USER1_EXAMPLE_COM).isEqualTo(userMessage.getEmailAddress());
        assertThat(FORMER_USER_1_EXAMPLE_COM).isEqualTo(userMessage.getFormerEmailAddress());

        assertThat(userMessage.getEmailAddress()).isEqualTo(NEW_MAIL_USER1_EXAMPLE_COM);
        assertThat(userMessage.getFormerEmailAddress()).isEqualTo(FORMER_USER_1_EXAMPLE_COM);
    }

    @Test
    void when_email_requested_to_change_already_in_use_throws_BadRequestException() {
        /* prepare */
        User user2 = new User();
        user2.emailAddress = NEW_MAIL_USER1_EXAMPLE_COM;
        when(userRepository.findByEmailAddress(NEW_MAIL_USER1_EXAMPLE_COM)).thenReturn(Optional.of(user2));

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userRequestUpdateMailAddress(NEW_MAIL_USER1_EXAMPLE_COM)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("The email address is already in use. Please chose another one.");
    }

    private User createKnownUser1() {
        User user = new User();
        user.name = KNOWN_USER1;
        user.emailAddress = FORMER_USER_1_EXAMPLE_COM;
        return user;
    }

}
