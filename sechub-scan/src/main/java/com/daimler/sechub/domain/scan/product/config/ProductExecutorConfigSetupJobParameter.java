package com.daimler.sechub.domain.scan.product.config;

public class ProductExecutorConfigSetupJobParameter {

    public static final String PROPERTY_KEY= "key";
    public static final String PROPERTY_VALUE= "value";
    
    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
