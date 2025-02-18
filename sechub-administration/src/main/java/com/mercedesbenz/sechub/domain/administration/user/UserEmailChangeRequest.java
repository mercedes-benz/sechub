// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Objects;

public record UserEmailChangeRequest(String userId, String newEmail) {
    public UserEmailChangeRequest {
        Objects.requireNonNull(userId, "userId may not be null");
        Objects.requireNonNull(newEmail, "newEmail may not be null");
    }
}
