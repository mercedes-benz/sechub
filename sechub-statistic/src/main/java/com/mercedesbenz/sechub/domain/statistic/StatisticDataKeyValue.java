package com.mercedesbenz.sechub.domain.statistic;

import java.math.BigInteger;

public class StatisticDataKeyValue{
    
    private StatisticDataKey key;
    private BigInteger value;
    
    public StatisticDataKeyValue(StatisticDataKey key,  long value) {
        this(key,BigInteger.valueOf(value));
    }
    
    public StatisticDataKeyValue(StatisticDataKey key,  BigInteger value) {
        this.key=key;
        this.value=value;
    }
    
    public StatisticDataKey getKey() {
        return key;
    }
    
    public BigInteger getValue() {
        return value;
    }
    
}