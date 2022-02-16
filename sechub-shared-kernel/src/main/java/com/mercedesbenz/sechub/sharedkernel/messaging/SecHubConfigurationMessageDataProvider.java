// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class SecHubConfigurationMessageDataProvider implements MessageDataProvider<SecHubConfiguration> {

    private static final SecHubConfiguration OBJECT = new SecHubConfiguration();

    @Override
    public SecHubConfiguration get(String data) {
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
    public String getString(SecHubConfiguration configuration) {
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
