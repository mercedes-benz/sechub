package com.mercedesbenz.sechub.sharedkernel.configuration;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;

@Component
public class MapToSecHubConfigurationMetaDataTransformer {

    public SecHubConfigurationMetaData transform(Map<String, String> allParameters) {
        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        for (String key: allParameters.keySet()) {
            if (key==null) {
                continue;
            }
            if (key.startsWith("metadata.labels.")){
                tryToAdoptMetaDataLabel(key, allParameters);
            }
        }
        
        return metaData;
    }

    private void tryToAdoptMetaDataLabel(String key, Map<String, String> allParameters) {
        
        
    }
}
