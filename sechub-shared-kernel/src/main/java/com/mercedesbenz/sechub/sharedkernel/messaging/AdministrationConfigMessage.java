// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.sharedkernel.MustBeKeptStable;

/**
 * This message data object contains all necessary information about
 * administration configuration change which can be interesting for messaging.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) scan,report, scheduler and administration")
public class AdministrationConfigMessage implements JSONable<AdministrationConfigMessage> {

    private long daysBeforeAutoCleanup;

    @Override
    public Class<AdministrationConfigMessage> getJSONTargetClass() {
        return AdministrationConfigMessage.class;
    }

    public long getDaysBeforeAutoCleanup() {
        return daysBeforeAutoCleanup;
    }

    public void setDaysBeforeAutoCleanup(long maximumAllowedDaysBefoerCleanup) {
        this.daysBeforeAutoCleanup = maximumAllowedDaysBefoerCleanup;
    }

}
