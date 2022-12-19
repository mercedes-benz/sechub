// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UUIDMessageDataProvider implements MessageDataProvider<UUID> {

    private static final Logger LOG = LoggerFactory.getLogger(UUIDMessageDataProvider.class);

    @Override
    public UUID get(String uuidAsString) {
        if (uuidAsString == null) {
            return null;
        }
        try {
            return UUID.fromString(uuidAsString);
        } catch (IllegalArgumentException e) {
            LOG.error("No UUID transformable because {} is not a valid UUID!", uuidAsString);
            return null;
        }
    }

    @Override
    public String getString(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }

}
