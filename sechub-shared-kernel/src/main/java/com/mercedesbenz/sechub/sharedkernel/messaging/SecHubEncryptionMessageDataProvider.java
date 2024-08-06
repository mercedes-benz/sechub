// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;

public class SecHubEncryptionMessageDataProvider implements MessageDataProvider<SecHubEncryptionData> {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubEncryptionMessageDataProvider.class);

    @Override
    public SecHubEncryptionData get(String encryptionDataAsJson) {
        if (encryptionDataAsJson == null) {
            return null;
        }
        try {
            return SecHubEncryptionData.fromString(encryptionDataAsJson);
        } catch (IllegalArgumentException e) {
            LOG.error("Cannot transform json to sechub encryption data", e);
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
