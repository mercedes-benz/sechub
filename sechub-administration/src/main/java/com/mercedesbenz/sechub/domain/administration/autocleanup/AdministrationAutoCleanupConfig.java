// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.autocleanup;

import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeUnit;

public class AdministrationAutoCleanupConfig implements JSONable<AdministrationAutoCleanupConfig> {

    private static final AdministrationAutoCleanupConfig CONVERTER = new AdministrationAutoCleanupConfig();

    CleanupTime cleanupTime = new CleanupTime();

    public CleanupTime getCleanupTime() {
        return cleanupTime;
    }

    public static class CleanupTime {

        CountableInDaysTimeUnit unit = CountableInDaysTimeUnit.MONTH;

        int amount;

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public void setUnit(CountableInDaysTimeUnit unit) {
            this.unit = unit;
        }

        public CountableInDaysTimeUnit getUnit() {
            return unit;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static AdministrationAutoCleanupConfig fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<AdministrationAutoCleanupConfig> getJSONTargetClass() {
        return AdministrationAutoCleanupConfig.class;
    }

}
