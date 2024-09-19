// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

public class ScheduleEncryptionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ScheduleEncryptionException(String description) {
        super(description);
    }
}
