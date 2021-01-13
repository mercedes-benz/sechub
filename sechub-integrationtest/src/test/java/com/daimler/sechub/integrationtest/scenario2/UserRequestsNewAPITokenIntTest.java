// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.USER_1;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

public class UserRequestsNewAPITokenIntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

	@Test
	public void an_anonymous_user_can_trigger_new_api_token_request_for_existing_user_email_adress_leads_to_mail_with_link_to_fetch_new_apitoken() {
	    /* check preconditions */
        String email = USER_1.getEmail();
        assertTrue(email.endsWith("_user1@example.org"));
        
		/* prepare */
		as(ANONYMOUS).requestNewApiTokenFor(USER_1.getEmail());

		/* execute receive of new api token*/
		String link = getLinkToFetchNewAPITokenAfterChangeRequest(USER_1);
		String apiToken = udpdateAPITokenByOneTimeTokenLink(USER_1, link);

		/* test */
		assertNotNull(apiToken);
		assertFalse(apiToken.isEmpty());
		assertEquals(USER_1.getApiToken(),apiToken);

	}


}
