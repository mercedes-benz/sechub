// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Checkmarx defines a few engine configurations.
 * 
 * This enum maps the Checkmarx engine configuration options to constant values.
 * 
 * The possible options can be found in the Checkmarx documentation:
 * - https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223543515/Get+All+Engine+Configurations+-+GET+sast+engineConfigurations+v8.6.0+and+up
 * 
 * Only the names of the engine configurations are mapped and not the ids, assuming Checkmarx can 
 * add/remove engineConfigurations or change the ids in the future.
 * 
 * @author Jeremias Eppler
 */

@Component
public class CheckmarxEngineConfigurationOptions {
   
    public static final String DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME = "Multi-language Scan";
    
    @Value("{sechub.adapter.checkmarx.engineconfiguration.name:"+DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME+"}")
    private String checkmarxName = DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME;
    
    CheckmarxEngineConfigurationOptions(String checkmarxName) {
        this.checkmarxName = checkmarxName;
    }
    
    /* getNameUsedForCheckmarxEngineConfigurationIDFetching*/
    public String getCheckmarxName() {
        return checkmarxName;
    }
}
