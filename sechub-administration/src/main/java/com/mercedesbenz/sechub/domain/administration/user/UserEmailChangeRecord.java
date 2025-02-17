// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Objects;

public record UserEmailChangeRecord(String userId, String newEmail) {
    public UserEmailChangeRecord {
        Objects.requireNonNull(userId, "userId may not be null");
        Objects.requireNonNull(newEmail, "newEmail may not be null");
    }
}
