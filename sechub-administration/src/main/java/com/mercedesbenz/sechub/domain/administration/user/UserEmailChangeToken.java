// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.mercedesbenz.sechub.commons.model.JSONable;

public class UserEmailChangeToken implements JSONable<UserEmailChangeToken> {

    private static final UserEmailChangeToken IMPORTER = new UserEmailChangeToken();
    private String emailAddress;
    private String userId;
    private String timestamp;

    public UserEmailChangeToken(String userId, String emailAddress, String timestamp) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.timestamp = timestamp;
        assertFields();
    }

    @JsonCreator
    private UserEmailChangeToken() {
        /* only for json import */
    }

    public static UserEmailChangeToken createFromJSON(String json) {
        return IMPORTER.fromJSON(json);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getUserId() {
        return userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public Class<UserEmailChangeToken> getJSONTargetClass() {
        return UserEmailChangeToken.class;
    }

    private void assertFields() {
        if (this.emailAddress == null || this.emailAddress.isBlank()) {
            throw new IllegalStateException("Email address must not be null or blank!");
        }
        if (this.userId == null || this.userId.isBlank()) {
            throw new IllegalStateException("User ID must not be null or blank!");
        }
        if (this.timestamp == null || this.timestamp.isBlank()) {
            throw new IllegalStateException("Timestamp must not be null or blank!");
        }
    }
}
