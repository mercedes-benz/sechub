// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SecHubTimeUnitData {
    private long time;
    private SecHubTimeUnit unit;
    
    private SecHubTimeUnitData() {}
    
    private SecHubTimeUnitData(int time, SecHubTimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }
    
    public static SecHubTimeUnitData of(int time, SecHubTimeUnit unit) {
        if (time <= 0) {
            throw new IllegalArgumentException("A time value of zero or a negative value is not accepted");
        }
        
        return new SecHubTimeUnitData(time, unit);
    }

    public long getTime() {
        return time;
    }

    public SecHubTimeUnit getUnit() {
        return unit;
    }
    
    @JsonIgnore
    public long getTimeInMilliseconds() {
        return time * unit.getMultiplicatorMilliseconds();
    }
    
    @JsonIgnore
    public long getTimeInHours() {
        return (long) Math.ceil(getTimeInMilliseconds() / (double)(60*60*1000));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(time, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecHubTimeUnitData other = (SecHubTimeUnitData) obj;
        return time == other.time && unit == other.unit;
    }
    
    @Override
    public String toString() {
        return "SecHubTimeUnitData [time=" + time + ", unit=" + unit + "]";
    }
}
