// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class JobMessageDataProvider implements MessageDataProvider<JobMessage> {

    private static final JobMessage OBJECT = new JobMessage();

    @Override
    public JobMessage get(String data) {
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
    public String getString(JobMessage message) {
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
