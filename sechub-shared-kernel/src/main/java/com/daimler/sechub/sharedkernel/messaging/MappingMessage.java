// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This message data object contains mapping information 
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This message is used by communication between scan domain and administration - and maybe others")
public class MappingMessage implements JSONable<MappingMessage> {
    
    private String mappingId;
    
    private MappingData mappingData;
    
    public void setMappingData(MappingData mappingData) {
        this.mappingData = mappingData;
    }
    
    public void setMappingId(String mappingIdentifier) {
        this.mappingId = mappingIdentifier;
    }
    
    public MappingData getMappingData() {
        return mappingData;
    }
    
    public String getMappingId() {
        return mappingId;
    }
    
    @Override
    public Class<MappingMessage> getJSONTargetClass() {
        return MappingMessage.class;
    }



}
