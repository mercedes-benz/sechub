// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

public class WebScanDurationConfiguration {
    private int duration;
    private SecHubTimeUnit unit;
    
    public int getDuration() {
        return duration;
    }
    
    public SecHubTimeUnit getUnit() {
        return unit;
    }
}
