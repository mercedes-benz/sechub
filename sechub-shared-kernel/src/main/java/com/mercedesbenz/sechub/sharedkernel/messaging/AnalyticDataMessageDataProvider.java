// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class AnalyticDataMessageDataProvider implements MessageDataProvider<AnalyticDataMessage> {

    private static final AnalyticDataMessage OBJECT = new AnalyticDataMessage();

    @Override
    public AnalyticDataMessage get(String data) {
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
    public String getString(AnalyticDataMessage message) {
        if (message == null) {
            return null;
        }
        try {
            return message.toJSON();
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }
    }

}
