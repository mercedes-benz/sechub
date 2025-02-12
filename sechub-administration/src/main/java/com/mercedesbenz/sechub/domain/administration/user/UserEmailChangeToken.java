// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;

public class UserEmailChangeToken {

    private String emailAddress;
    private String userId;
    private String timestamp;

    public UserEmailChangeToken(String userId, String emailAddress, String timestamp) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.timestamp = timestamp;
    }

    public UserEmailChangeToken() {
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

    public String toJSON() {
        return JSONConverter.get().toJSON(this);
    }

    public static UserEmailChangeToken fromJSON(String json) {
        return JSONConverter.get().fromJSON(UserEmailChangeToken.class, json);
    }

    public void validate() {
        if (this.emailAddress == null || this.emailAddress.isBlank()) {
            throw new NotAcceptableException("Email address must not be null or blank!");
        }
        if (this.userId == null || this.userId.isBlank()) {
            throw new NotAcceptableException("User ID must not be null or blank!");
        }
        if (this.timestamp == null || this.timestamp.isBlank()) {
            throw new NotAcceptableException("Timestamp must not be null or blank!");
        }
    }
}
