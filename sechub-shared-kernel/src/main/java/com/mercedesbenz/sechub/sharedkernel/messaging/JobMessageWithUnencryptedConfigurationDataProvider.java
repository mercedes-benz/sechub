// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class JobMessageWithUnencryptedConfigurationDataProvider implements MessageDataProvider<JobMessageWithUnencryptedConfiguration> {

    private static final JobMessageWithUnencryptedConfiguration OBJECT = new JobMessageWithUnencryptedConfiguration();

    @Override
    public JobMessageWithUnencryptedConfiguration get(String data) {
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
    public String getString(JobMessageWithUnencryptedConfiguration message) {
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
