// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.integrationtest.internal.TestScenario;
import com.daimler.sechub.test.ExampleConstants;

public class TestUser implements UserContext {

    private String userIdPart;
    private String apiToken;
    private String email;
    private String prefix;
    private String specialMailAddress;

    private static final List<TestUser> all = new ArrayList<>();

    TestUser() {
        all.add(this);
    }

    public TestUser(String userIdPart, String apiToken) {
        this(userIdPart, apiToken, null);
    }

    public TestUser(String userIdPart, String apiToken, String specialMailAddress) {
        this.userIdPart = userIdPart;
        this.apiToken = apiToken;
        this.specialMailAddress = specialMailAddress;
    }

    public void prepare(TestScenario scenario) {
        this.prefix = scenario.getName().toLowerCase();
    }

    public String getUserId() {
        return prefix + "_" + userIdPart;
    }

    public String getApiToken() {
        return apiToken;
    }

    public boolean isAnonymous() {
        return userIdPart == null || userIdPart.isEmpty();
    }

    public void updateToken(String newToken) {
        if (isAnonymous()) {
            throw new IllegalStateException("anonymous users may not have token updated!");
        }
        this.apiToken = newToken;
    }

    public String getEmail() {
        if (specialMailAddress != null) {
            return specialMailAddress;
        }
        return getUserId() + "@" + ExampleConstants.URI_TARGET_SERVER;
    }

    @Override
    public String toString() {
        return "TestUser [userId=" + getUserId() + ", apiToken=" + apiToken + ", email=" + email + "]";
    }

    /**
     * Create a clone of this instance, but with uppercased user id
     *
     * @return new test user object - same as origin (mail, apitoken) but user id is
     *         uppercased
     */
    public TestUser clonedButWithUpperCasedId() {
        TestUser copy = new TestUser(userIdPart.toUpperCase(), getApiToken());
        copy.prefix = this.prefix; // restore data from prepare of origin
        return copy;
    }

}
