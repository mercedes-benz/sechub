package com.daimler.sechub.sharedkernel.configuration;

import java.util.Optional;

public class WebScanDurationConfiguration {
    public static final String PROPERTY_TIME = "duration";
    public static final String PROPERTY_UNIT = "unit";

    private Optional<Long> duration = Optional.empty();
    private Optional<String> unit = Optional.empty();
    
    public Optional<Long> getDuration() {
        return duration;
    }
    
    public Optional<String> getUnit() {
        return unit;
    }
}
