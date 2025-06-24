// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class MapStringToListOfStringsMessageDataProvider implements MessageDataProvider<Map<String, List<String>>> {

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<String>> get(String projectToProfiles) {
        if (projectToProfiles == null || projectToProfiles.isBlank()) {
            return null;
        }
        try {
            return JSONConverter.get().fromJSON(Map.class, projectToProfiles);
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }
    }

    @Override
    public String getString(Map<String, List<String>> projectToProfiles) {
        if (projectToProfiles == null) {
            return null;
        }
        try {
            return JSONConverter.get().toJSON(projectToProfiles);
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }
    }
}
