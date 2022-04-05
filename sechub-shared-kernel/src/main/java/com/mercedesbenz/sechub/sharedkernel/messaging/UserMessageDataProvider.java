// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class UserMessageDataProvider implements MessageDataProvider<UserMessage> {

    private static final UserMessage OBJECT = new UserMessage();

    @Override
    public UserMessage get(String data) {
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
    public String getString(UserMessage configuration) {
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
