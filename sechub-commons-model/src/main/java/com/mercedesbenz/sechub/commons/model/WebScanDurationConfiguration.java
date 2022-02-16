// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public class WebScanDurationConfiguration {
    public static final String PROPERTY_DURATION = "duration";
    public static final String PROPERTY_UNIT = "unit";

    private int duration;
    private SecHubTimeUnit unit;

    public int getDuration() {
        return duration;
    }

    public SecHubTimeUnit getUnit() {
        return unit;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setUnit(SecHubTimeUnit unit) {
        this.unit = unit;
    }
}
