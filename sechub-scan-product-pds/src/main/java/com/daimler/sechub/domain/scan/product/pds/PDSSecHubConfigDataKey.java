// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

public abstract class PDSSecHubConfigDataKey<T extends PDSSecHubConfigDataKey<?>> {

    private String id;
    private String description;
    private boolean mandatory;
    private boolean sentToPDS;
    private boolean generated;

    PDSSecHubConfigDataKey(String key, String description) {
        this.id = key.toLowerCase();
        this.description = description;
    }
    
    @SuppressWarnings("unchecked")
    public T markMandatory() {
        this.mandatory=true;
        return (T) this;
    }

    /*
     * Mark this key as generated, means it will be automatically created and sent on PDS calls
     */
    @SuppressWarnings("unchecked")
    public T markGenerated() {
        this.generated=true;
        return (T) this;
    }
    
    public String getId() {
        return id;
    }
    
    public boolean isGenerated() {
        return generated;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public String getDescription() {
        return description;
    }
    
    @SuppressWarnings("unchecked")
    T markAlwaysSentToPDS() {
        this.sentToPDS=true;
        return (T) this;
    }
    
    public boolean isSentToPDS() {
        return sentToPDS;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+ "[" + (id != null ? "id=" + id + ", " : "") + "mandatory=" + mandatory + "]";
    } 
    
}