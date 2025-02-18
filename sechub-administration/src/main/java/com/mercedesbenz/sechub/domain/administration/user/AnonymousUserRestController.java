// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.web.bind.annotation.*;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAnonymousUserVerifiesEmailAddress;
import com.mercedesbenz.sechub.spring.security.SecHubSecurityProperties;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AnonymousUserRestController {

    private final UserEmailAddressUpdateService emailAddressUpdateService;
    private final SecHubSecurityProperties secHubSecurityProperties;
    private final String REDIRECT_URI;
    // todo: we should define a default redirect uri which is always available
    // (/login is not)
    private final String DEFAULT_REDIRECT_URI = "/api/anonymous/check/alive";
    private final String FRONTEND_REDIRECT_URI_PATH = "/user/email_verify";

    AnonymousUserRestController(UserEmailAddressUpdateService emailAddressUpdateService, SecHubSecurityProperties secHubSecurityProperties) {
        this.emailAddressUpdateService = emailAddressUpdateService;
        this.secHubSecurityProperties = secHubSecurityProperties;
        REDIRECT_URI = getRedirectUri(secHubSecurityProperties);
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
        response.sendRedirect(REDIRECT_URI);
    }

    private String getRedirectUri(SecHubSecurityProperties secHubSecurityProperties) {
        SecHubSecurityProperties.LoginProperties loginProperties = secHubSecurityProperties.getLoginProperties();
        if (loginProperties == null) {
            return DEFAULT_REDIRECT_URI;
        }

        String redirectUri = loginProperties.getRedirectUri();
        if (redirectUri == null) {
            return DEFAULT_REDIRECT_URI;
        }

        try {
            String baseUrl = getBaseUri(redirectUri);
            return baseUrl + FRONTEND_REDIRECT_URI_PATH;
        } catch (URISyntaxException e) {
            return DEFAULT_REDIRECT_URI;
        }
    }

    private String getBaseUri(String fullUri) throws URISyntaxException {
        // todo if we agree on redirectUri to be the root of the web-ui server, we can
        // remove this method
        URI uri = new URI(fullUri);

        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();

        // Construct the base URL
        String baseUrl = scheme + "://" + host;
        if (port != -1 && port != 80 && port != 443) {
            baseUrl += ":" + port;
        }

        return baseUrl;
    }
}
