// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class AnalyticMessageDataProvider implements MessageDataProvider<AnalyticMessageData> {

    private static final AnalyticMessageData OBJECT = new AnalyticMessageData();

    @Override
    public AnalyticMessageData get(String data) {
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
    public String getString(AnalyticMessageData message) {
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
