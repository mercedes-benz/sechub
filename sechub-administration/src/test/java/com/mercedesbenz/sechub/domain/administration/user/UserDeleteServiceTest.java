// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class UserDeleteServiceTest {

    private static final String OTHER_USER = "otheruser";
    private static final String ADMIN_HIMSELF = "iammyself";
    private UserDeleteService serviceToTest;
    private UserContextService userContext;
    private DomainMessageService eventBusService;
    private UserRepository userRepository;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    private AuditLogService auditLogService;

    @Before
    public void before() throws Exception {
        eventBusService = mock(DomainMessageService.class);
        userContext = mock(UserContextService.class);
        userRepository = mock(UserRepository.class);
        auditLogService = mock(AuditLogService.class);

        serviceToTest = new UserDeleteService();
        serviceToTest.eventBusService = eventBusService;
        serviceToTest.userContext = userContext;
        serviceToTest.userRepository = userRepository;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
    }

    @Test
    public void it_its_not_allowed_to_delete_yourself() {
        /* test */
        expected.expect(NotAcceptableException.class);

        /* prepare */
        when(userContext.getUserId()).thenReturn(ADMIN_HIMSELF);
        when(userRepository.findOrFailUser(ADMIN_HIMSELF)).thenReturn(null); // it does not matter - check is done before!

        /* execute */
        serviceToTest.deleteUser(ADMIN_HIMSELF);

    }

    @Test
    public void it_is_allowed_to_delete_another_user() {

        /* prepare */
        User otherUser = new User();
        otherUser.name = OTHER_USER;

        when(userContext.getUserId()).thenReturn(ADMIN_HIMSELF);
        when(userRepository.findOrFailUser(OTHER_USER)).thenReturn(otherUser); // other user found

        /* execute */
        serviceToTest.deleteUser(OTHER_USER);

        /* test */
        // just no exception

    }

}
