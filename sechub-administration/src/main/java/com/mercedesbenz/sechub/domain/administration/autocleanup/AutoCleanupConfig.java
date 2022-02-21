package com.mercedesbenz.sechub.domain.administration.autocleanup;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class AutoCleanupConfig implements JSONable<AutoCleanupConfig> {

    private static final AutoCleanupConfig CONVERTER = new AutoCleanupConfig();

    CleanupTime cleanupTime = new CleanupTime();

    public CleanupTime getCleanupTime() {
        return cleanupTime;
    }

    public class CleanupTime {
        CountableInDaysTimeunit unit = CountableInDaysTimeunit.MONTH;

        int amount = 3;

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public void setUnit(CountableInDaysTimeunit unit) {
            this.unit = unit;
        }

        public CountableInDaysTimeunit getUnit() {
            return unit;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static AutoCleanupConfig fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<AutoCleanupConfig> getJSONTargetClass() {
        return AutoCleanupConfig.class;
    }

}
