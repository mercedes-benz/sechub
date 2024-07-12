// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@MustBeKeptStable("used in database as values")
public enum CipherPasswordSourceType {

    NONE,

    ENVIRONMENT_VARIABLE
}
