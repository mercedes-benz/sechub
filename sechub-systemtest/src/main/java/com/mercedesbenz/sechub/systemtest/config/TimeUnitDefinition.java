// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.concurrent.TimeUnit;

public class TimeUnitDefinition extends AbstractDefinition {

    TimeUnitDefinition() {
        /* necessary for JSON */
    }

    public TimeUnitDefinition(int amount, TimeUnit unit) {
        /* necessary for defaults */
        this.amount = amount;
        this.unit = unit;
    }

    private int amount;
    private TimeUnit unit;

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
