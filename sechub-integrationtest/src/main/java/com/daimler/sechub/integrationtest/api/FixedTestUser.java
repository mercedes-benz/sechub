// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.TestScenario;

/**
 * A special test user variant - the user id and the API token will not be
 * changed by scenarios. It is even possible to setup a fixed email address.
 * Only necessary for some special purposes - e.g. inside DAUI where test
 * framework methods are used having TestUser arguments.
 *
 * @author Albert Tregnaghi
 *
 */
public class FixedTestUser extends TestUser {

    private String fixedUserId;

    public FixedTestUser() {
        super();
    }

    public FixedTestUser(String userId, String apiToken, String specialMailAddress) {
        super("nopart", apiToken, specialMailAddress);
        this.fixedUserId = userId;
    }

    public FixedTestUser(String userId, String apiToken) {
        this(userId, apiToken, null);
    }

    @Override
    public String getUserId() {
        return fixedUserId;
    }

    @Override
    public void prepare(TestScenario scenario) {
        /* we do not prepare fixed ones */
    }

}
