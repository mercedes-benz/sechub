// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

/**
 * This message data object contains all necessary information about
 * administration configuration change which can be interesting for messaging.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) scan, report, scheduler and administration")
public class AdministrationConfigMessage implements JSONable<AdministrationConfigMessage> {

    private long autoCleanupInDays;

    @Override
    public Class<AdministrationConfigMessage> getJSONTargetClass() {
        return AdministrationConfigMessage.class;
    }

    public long getAutoCleanupInDays() {
        return autoCleanupInDays;
    }

    public void setAutoCleanupInDays(long maximumAllowedDaysBefoerCleanup) {
        this.autoCleanupInDays = maximumAllowedDaysBefoerCleanup;
    }

}
