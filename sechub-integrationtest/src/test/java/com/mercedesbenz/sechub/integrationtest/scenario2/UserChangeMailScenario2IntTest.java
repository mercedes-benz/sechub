// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.getLinktToVerifyEmailAddressAfterChangeRequest;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.USER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestUserDetailInformation;

public class UserChangeMailScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Test
    public void user_request_change_mail_address() {
        /* prepare */
        String email = USER_1.getEmail();
        String newEmail = "new_user1@example.org";
        assertTrue(email.endsWith("_user1@example.org"));

        /* execute */
        as(USER_1).requestChangeMailAddressTo(newEmail);

        /* test */
        String link = getLinktToVerifyEmailAddressAfterChangeRequest(newEmail, USER_1);
        assertThat(link).isNotNull();

        // workaround to avoid double encoding of the link
        URI uri = null;
        try {
            uri = new URI(link);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateEmailByOneTimeTokenLink(uri);
        TestUserDetailInformation testUserDetailInformation = as(SUPER_ADMIN).fetchUserDetails(USER_1);
        assertThat(testUserDetailInformation.getEmail()).isEqualTo(newEmail);
    }

}
