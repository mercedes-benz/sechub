package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportScan {

    private long total = 0;
    private long red = 0;
    private long yellow = 0;
    private long green = 0;

    public long getTotal() {
        return total;
    }

    public long getRed() {
        return red;
    }

    public long getYellow() {
        return yellow;
    }

    public long getGreen() {
        return green;
    }
}
