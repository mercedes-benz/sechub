package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class SecHubOpenAPIConfiguration implements SecHubDataConfigurationUsageByName{
    
    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();
    
    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }

   

}
