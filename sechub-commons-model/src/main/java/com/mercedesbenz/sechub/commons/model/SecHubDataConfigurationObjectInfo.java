// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public class SecHubDataConfigurationObjectInfo {

    SecHubDataConfigurationType type;
    SecHubDataConfigurationObject dataConfigurationObject;
    
    public SecHubDataConfigurationObject getDataConfigurationObject() {
        return dataConfigurationObject;
    }
    
    public SecHubDataConfigurationType getType() {
        return type;
    }
}
