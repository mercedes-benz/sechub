// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario1.Scenario1.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class UserRegistrationScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Registration . ...........................+ */
    /* +-----------------------------------------------------------------------+ */
    @Test
    public void an_existing_signup_is_not_added_twice() {
        /* @formatter:off */
		as(ANONYMOUS).signUpAs(USER_1);
		assertSignup(USER_1).doesExist();

		/* execute + test */
		expectHttpFailure(()->as(ANONYMOUS).signUpAs(USER_1), HttpStatus.NOT_ACCEPTABLE);

		/* @formatter:on */
    }

    @Test
    public void an_unregistered_user_can_be_accepted_by_admin_gets_a_link_and_is_then_registered() {
        /* check precondition */
        assertUser(USER_1).doesNotExist();

        /* prepare */
        String json = """
                {
                    "apiVersion": "1.0",
                    "userId": "%s",
                    "emailAddress": "%s",
                    "some-unknown-value": "unknown"
                }
                """.formatted(USER_1.getUserId(), USER_1.getEmail());
        as(ANONYMOUS).signUpWithJson(json);
        assertSignup(USER_1).doesExist();
        assertUser(USER_1).doesNotExist(); // still not existing

        /* execute */
        as(SUPER_ADMIN).acceptSignup(USER_1);

        /* test */
        assertUser(USER_1).doesExist();

        /* execute receive of new api token */
        String link = getLinkToFetchNewAPITokenAfterSignupAccepted(USER_1);
        String apiToken = udpdateAPITokenByOneTimeTokenLink(USER_1, link);

        /* test */
        assertNotNull(apiToken);
        assertFalse(apiToken.isEmpty());
        assertEquals(USER_1.getApiToken(), apiToken);

    }

}
