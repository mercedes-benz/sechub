// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;

public class SecHubEncryptionMessageDataProvider implements MessageDataProvider<SecHubEncryptionData> {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubEncryptionMessageDataProvider.class);

    @Override
    public SecHubEncryptionData get(String uuidAsString) {
        if (uuidAsString == null) {
            return null;
        }
        try {
            return SecHubEncryptionData.fromString(uuidAsString);
        } catch (IllegalArgumentException e) {
            LOG.error("No UUID transformable because {} is not a valid UUID!", uuidAsString);
            return null;
        }
    }

    @Override
    public String getString(SecHubEncryptionData data) {
        if (data == null) {
            return null;
        }
        return data.toJSON();
    }

}
