// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.email;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

/**
 * This is only availabe in mocked_notification profile. Interesting for
 * integration tests to get the emails send...
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.MOCKED_NOTIFICATIONS)
public class MockEmailRestController {

    @Autowired
    MockEmailService mockMailService;

    /* @formatter:off */
	@RequestMapping(
			path = APIConstants.API_ANONYMOUS+"integrationtest/mock/emails/to/{emailAdress}",
			method = RequestMethod.GET,
			produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public List<SimpleMailMessage> getMailsFor(@PathVariable(name="emailAdress") String emailAdress) {
		/* @formatter:on */
        return mockMailService.getMailsFor(emailAdress);
    }

    /* @formatter:off */
	@RequestMapping(
			path = APIConstants.API_ANONYMOUS+"integrationtest/mock/emails",
			method = RequestMethod.DELETE,
			produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void resetMockMails() {
		/* @formatter:on */
        mockMailService.resetMockMails();
    }
}
