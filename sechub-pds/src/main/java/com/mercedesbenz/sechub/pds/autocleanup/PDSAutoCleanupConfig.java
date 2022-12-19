// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeUnit;

public class PDSAutoCleanupConfig implements JSONable<PDSAutoCleanupConfig> {

    private static final PDSAutoCleanupConfig CONVERTER = new PDSAutoCleanupConfig();

    CleanupTime cleanupTime = new CleanupTime();

    public CleanupTime getCleanupTime() {
        return cleanupTime;
    }

    public class CleanupTime {

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

    public static PDSAutoCleanupConfig fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<PDSAutoCleanupConfig> getJSONTargetClass() {
        return PDSAutoCleanupConfig.class;
    }

}
