// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAnonymousUserVerifiesEmailAddress;
import com.mercedesbenz.sechub.spring.security.SecHubSecurityProperties;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AnonymousUserRestController {
    private static final Logger LOG = LoggerFactory.getLogger(AnonymousUserRestController.class);
    private final UserEmailAddressUpdateService emailAddressUpdateService;
    private final SecHubSecurityProperties secHubSecurityProperties;
    private final String redirectUri;
    // Frontend URI path to success hidden page
    private final String FRONTEND_REDIRECT_URI_PATH = "/user/email_verify";

    AnonymousUserRestController(UserEmailAddressUpdateService emailAddressUpdateService, SecHubSecurityProperties secHubSecurityProperties) {
        this.emailAddressUpdateService = emailAddressUpdateService;
        this.secHubSecurityProperties = secHubSecurityProperties;
        redirectUri = getRedirectUri(secHubSecurityProperties);
    }

    /* @formatter:off*/
    @UseCaseAnonymousUserVerifiesEmailAddress(
            @Step(
                    number = 1,
                    name = "Rest call",
                    next = {2},
                    description = "User verifies his new email address",
                    needsRestDoc = true))
    @RequestMapping(value = AdministrationAPIConstants.API_ANONYMOUS_USER_VERIFY_EMAIL + "/{oneTimeToken}", method = RequestMethod.GET)
    public void verifyEmailAddress(@PathVariable(name = "oneTimeToken") String oneTimeToken, HttpServletResponse response) throws IOException {
        /* @formatter:on */
        emailAddressUpdateService.userVerifiesUserEmailAddress(oneTimeToken);
        if (redirectUri == null || redirectUri.isEmpty()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            response.sendRedirect(redirectUri);
        }
    }

    private String getRedirectUri(SecHubSecurityProperties secHubSecurityProperties) {
        SecHubSecurityProperties.LoginProperties loginProperties = secHubSecurityProperties.getLoginProperties();
        if (loginProperties == null) {
            return "";
        }

        String redirectBaseUri = loginProperties.getRedirectUri();
        if (redirectBaseUri == null || redirectBaseUri.isBlank()) {
            return "";
        }

        redirectBaseUri = redirectBaseUri + FRONTEND_REDIRECT_URI_PATH;
        assertUri(redirectBaseUri);

        return redirectBaseUri;
    }

    private void assertUri(String redirectBaseUri) {
        try {
            new URI(redirectBaseUri);
        } catch (Exception e) {
            throw new IllegalStateException("Redirect URI is not a valid URI: " + redirectBaseUri);
        }
    }

}
