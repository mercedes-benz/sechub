// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class SecHubMessagesListDataProvider implements MessageDataProvider<SecHubMessagesList> {

    @Override
    public SecHubMessagesList get(String data) {
        if (data == null) {
            return null;
        }
        try {
            return SecHubMessagesList.fromJSONString(data);
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }

    }

    @Override
    public String getString(SecHubMessagesList configuration) {
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
