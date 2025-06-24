// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static java.util.Objects.requireNonNull;

public record UserEmailChangeRequest(String userId, String newEmail) {
    public UserEmailChangeRequest {
        requireNonNull(userId, "userId may not be null");
        requireNonNull(newEmail, "newEmail may not be null");
    }
}
