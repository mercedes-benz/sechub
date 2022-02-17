// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mercedesbenz.sechub.domain.administration.APITokenGenerator;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class AnonymousUserGetAPITokenByOneTimeTokenServiceTest {

    private static final String NEW_API_TOKEN_GENERATED = "new-api-token-generated";
    private AnonymousUserGetAPITokenByOneTimeTokenService serviceToTest;
    private UserInputAssertion assertion;
    private UserRepository userRepository;
    private User userTokenOutDated;
    private User userTokenValid;
    private APITokenGenerator apiTokenGenerator;

    @Before
    public void before() throws Exception {
        serviceToTest = new AnonymousUserGetAPITokenByOneTimeTokenService();

        userRepository = mock(UserRepository.class);
        assertion = mock(UserInputAssertion.class);
        apiTokenGenerator = mock(APITokenGenerator.class);

        /* prepare */
        serviceToTest.assertion = assertion;
        serviceToTest.sechubUserRepository = userRepository;
        serviceToTest.eventBusService = mock(DomainMessageService.class);
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.securityLogService = mock(SecurityLogService.class);
        serviceToTest.passwordEncoder = mock(PasswordEncoder.class);
        serviceToTest.apiTokenGenerator = apiTokenGenerator;

        userTokenOutDated = mock(User.class);
        when(userTokenOutDated.isOneTimeTokenOutDated(AnonymousUserGetAPITokenByOneTimeTokenService.DEFAULT_OUTDATED_TIME_MILLIS)).thenReturn(true);

        userTokenValid = mock(User.class);

        when(apiTokenGenerator.generateNewAPIToken()).thenReturn(NEW_API_TOKEN_GENERATED);
        when(userRepository.findByOneTimeToken(eq("invalid-token-user-not-found"))).thenReturn(Optional.ofNullable(null));
        when(userRepository.findByOneTimeToken(eq("valid-onetime-token"))).thenReturn(Optional.of(userTokenValid));
        when(userRepository.findByOneTimeToken(eq("valid-onetime-token-but-outdated"))).thenReturn(Optional.of(userTokenOutDated));

    }

    @Test
    public void valid_token_results_in_token_output() {
        /* execute */
        String result = serviceToTest.createNewAPITokenForUserByOneTimeToken("valid-onetime-token");

        /* test */
        assertEquals(NEW_API_TOKEN_GENERATED, result);

    }

    @Test
    public void valid_token_but_outdated_results_in_default_message() {
        /* execute */
        String result = serviceToTest.createNewAPITokenForUserByOneTimeToken("valid-onetime-token-but-outdated");

        /* test */
        assertEquals(AnonymousUserGetAPITokenByOneTimeTokenService.ANSWER_WHEN_TOKEN_CANNOT_BE_CHANGED, result);

    }

    @Test
    public void user_not_found_results_in_default_message() {
        /* execute */
        String result = serviceToTest.createNewAPITokenForUserByOneTimeToken("invalid-token-user-not-found");

        /* test */
        assertEquals(AnonymousUserGetAPITokenByOneTimeTokenService.ANSWER_WHEN_TOKEN_CANNOT_BE_CHANGED, result);

    }

}
