// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.AssertMail.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;
import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TestUserDetailInformation;
import com.mercedesbenz.sechub.integrationtest.api.TextSearchMode;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario2.class)
public class UserChangeMailScenario2IntTest {

    @Test
    void user_request_change_mail_address() throws URISyntaxException {
        /* check preconditions */
        String email = USER_1.getEmail();
        assertThat(email).endsWith("_user1@example.org");

        /* prepare */
        String newEmail = createRandomNewMailAddressForUser1();

        /* execute 1 - request change */
        as(USER_1).requestChangeMailAddressTo(newEmail);

        /* execute 2 - "click" on the link from the received email */
        URI link = assertAndFetchLinkToVerifyEmailAddressAfterChangeRequest(newEmail, USER_1);
        updateEmailByOneTimeTokenLink(link);

        /* test */
        TestUserDetailInformation testUserDetailInformation = as(SUPER_ADMIN).fetchUserDetails(USER_1);
        assertThat(testUserDetailInformation.getEmail()).isEqualTo(newEmail);

        // check mails have been sent
        assertMailExists(newEmail, "has been changed to this address", TextSearchMode.CONTAINS);
        assertMailExists(email, "SecHub account email address changed", TextSearchMode.CONTAINS);
    }

    private String createRandomNewMailAddressForUser1() {
        // create a random email address - means we can restart the integration test
        // locally without
        // getting a BAD_REQUEST because email address already in use ...
        return "new_mail_address_for_user1_" + UUID.randomUUID() + "@example.org";
    }

}
