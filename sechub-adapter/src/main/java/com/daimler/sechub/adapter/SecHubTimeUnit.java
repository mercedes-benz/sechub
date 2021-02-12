package com.daimler.sechub.adapter;

public enum SecHubTimeUnit {
    MILLISECOND(1),
    SECOND(1000),
    MINUTE(1000*60),
    HOUR(1000*60*60),
    DAY(1000*60*60*24);
    
    private int multiplicatorMilliseconds;

    SecHubTimeUnit(int multiplicatorMilliseconds) {
        this.multiplicatorMilliseconds = multiplicatorMilliseconds;
    }
    
    public long getMultiplicatorMilliseconds() {
        return multiplicatorMilliseconds;
    }
    
    public static SecHubTimeUnit valueOfUnit(String timeUnit) {
        SecHubTimeUnit unit = null;
        
        if (timeUnit != null) {
            String timeUnitLower = timeUnit.toLowerCase();

            switch (timeUnitLower) {
                case "millisecond":
                case "milliseconds":
                    unit = MILLISECOND;
                    break;
                case "second":
                case "seconds":
                    unit = SECOND;
                    break;
                case "minute":
                case "minutes":
                    unit = MINUTE;
                    break;
                case "hour":
                case "hours":
                    unit = HOUR;
                    break;
                case "day":
                case "days":
                    unit = DAY;
                    break;
                default:
                    break;
            }
            
            if (unit == null) {
                String message = String.format("A time unit of \"%s\" is not an accepted time unit.", timeUnit);
                throw new IllegalArgumentException(message);
            }
        } else {
            throw new NullPointerException("The time unit cannot be null.");
        }
            
        return unit;
    }
}
