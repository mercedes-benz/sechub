// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

public abstract class PDSSecHubConfigDataKey<T extends PDSSecHubConfigDataKey<?>> {

    private String id;
    private String description;
    private boolean mandatory;
    private boolean sentToPDS;
    private boolean generated;

    /**
     * Creates a new config data key
     * @param id identifier may never be <code>null</code>
     * @param description
     */
    PDSSecHubConfigDataKey(String id, String description) {
        notNull(id, "Configuration data key identifier may not be null!");
        
        this.id = id.toLowerCase();
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
    
    /**
     * @return identifier, never <code>null</code>
     */
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