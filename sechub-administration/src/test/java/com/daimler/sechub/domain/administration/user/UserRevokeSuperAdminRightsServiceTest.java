// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.domain.Example;

import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class UserRevokeSuperAdminRightsServiceTest {

	private UserRevokeSuperAdminRightsService serviceToTest;

	private SecHubEnvironment environment;
	private AuditLogService auditLogService;
	private DomainMessageService eventBusService;
	private UserRepository userRepository;

	private String ADMIN_USER;

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();

	@Before
	public void before() throws Exception {
		environment = mock(SecHubEnvironment.class);
		auditLogService = mock(AuditLogService.class);
		eventBusService = mock(DomainMessageService.class);
		userRepository = mock(UserRepository.class);

		User superUser = new User();
		superUser.superAdmin = true;
		superUser.deactivated = false;
		superUser.name = ADMIN_USER;

		when(userRepository.findOrFailUser(eq(ADMIN_USER))).thenReturn(superUser);

		serviceToTest = new UserRevokeSuperAdminRightsService();
		serviceToTest.secHubEnvironment = environment;
		serviceToTest.auditLogService = auditLogService;
		serviceToTest.eventBusService = eventBusService;
		serviceToTest.userRepository = userRepository;
		serviceToTest.logSanitizer=mock(LogSanitizer.class);
		serviceToTest.assertion=mock(UserInputAssertion.class);

	}

	@Test
	public void when_last_admin_user_rights_cannot_be_revoked() {
		/* prepare */
		User exampleUser = new User();
		exampleUser.superAdmin = true;
		when(userRepository.count(eq(Example.of(exampleUser)))).thenReturn(1L); // query counts super admins now with 1

		/* test */
		expected.expect(NotAcceptableException.class);

		/* execute */
		serviceToTest.revokeSuperAdminRightsFrom(ADMIN_USER);
	}

	@Test
	public void when_not_last_admin_user_rights_can_be_revoked() {
		/* prepare */
		User capture = new User();
		capture.superAdmin = true;
		when(userRepository.count(eq(Example.of(capture)))).thenReturn(2L); // query counts super admins now with 2, so can revoke


		/* execute */
		serviceToTest.revokeSuperAdminRightsFrom(ADMIN_USER);

		/* test */
		// just no exception
	}

}
