package com.daimler.sechub.sharedkernel.configuration;

import com.daimler.sechub.adapter.SecHubTimeUnit;

public class WebScanDurationConfiguration {
    public static final String PROPERTY_TIME = "duration";
    public static final String PROPERTY_UNIT = "unit";

    private int duration;
    private SecHubTimeUnit unit;
    
    public int getDuration() {
        return duration;
    }
    
    public SecHubTimeUnit getUnit() {
        return unit;
    }
}
