// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.TestScenario;

public class FixedTestUser extends TestUser{

    private String fixedUserId;
    public FixedTestUser() {
        super();
    }

    public FixedTestUser(String userId, String apiToken, String specialMailAddress) {
        super("nopart", apiToken, specialMailAddress);
        this.fixedUserId=userId;
    }

    public FixedTestUser(String userIdPart, String apiToken) {
        super(userIdPart, apiToken);
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
