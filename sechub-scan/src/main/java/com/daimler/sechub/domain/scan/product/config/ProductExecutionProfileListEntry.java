// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

/**
 * A reduced view to executor configurations - contains reduced configuration data for list
 * 
 * @author Albert Tregnaghi
 *
 */
public class ProductExecutionProfileListEntry {

    String id;

    String description;
    
    Boolean enabled;
    
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }

}
