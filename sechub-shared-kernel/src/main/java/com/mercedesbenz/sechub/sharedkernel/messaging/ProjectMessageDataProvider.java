// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class ProjectMessageDataProvider implements MessageDataProvider<ProjectMessage> {

    private static final ProjectMessage OBJECT = new ProjectMessage();

    @Override
    public ProjectMessage get(String data) {
        if (data == null) {
            return null;
        }
        try {
            return OBJECT.fromJSON(data);
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }

    }

    @Override
    public String getString(ProjectMessage configuration) {
        if (configuration == null) {
            return null;
        }
        try {
            return configuration.toJSON();
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }
    }

}
