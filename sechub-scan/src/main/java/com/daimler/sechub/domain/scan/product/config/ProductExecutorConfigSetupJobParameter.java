// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.Objects;

public class ProductExecutorConfigSetupJobParameter {

    public static final String PROPERTY_KEY= "key";
    public static final String PROPERTY_VALUE= "value";
    
    public ProductExecutorConfigSetupJobParameter() {
    }
    
    public ProductExecutorConfigSetupJobParameter(String key, String value) {
        this.key=key;
        this.value=value;
    }
    
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

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProductExecutorConfigSetupJobParameter other = (ProductExecutorConfigSetupJobParameter) obj;
        return Objects.equals(key, other.key);
    }

    @Override
    public String toString() {
        return "ProductExecutorConfigSetupJobParameter [" + (key != null ? "key=" + key + ", " : "") + (value != null ? "value=" + value : "") + "]";
    }
    
    
}
