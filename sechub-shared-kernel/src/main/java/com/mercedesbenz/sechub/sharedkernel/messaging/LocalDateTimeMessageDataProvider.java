// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDateTimeMessageDataProvider implements MessageDataProvider<LocalDateTime> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalDateTimeMessageDataProvider.class);

    @Override
    public LocalDateTime get(String iso8601timeFormat) {
        if (iso8601timeFormat == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(iso8601timeFormat);
        } catch (DateTimeException e) {
            LOG.error("No LocalDateTime transformable because {} is not a valid iso8601 time format!", iso8601timeFormat, e);
            return null;
        }
    }

    @Override
    public String getString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toString();
    }

}
