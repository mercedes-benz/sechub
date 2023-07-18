// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializes {@link LocalDateTime} objects with the sechub time date pattern:
 * {@value SecHubDateTimeFormat#PATTERN} - ISO 8601 with nanoseconds and UTC.
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    public SecHubLocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException, JsonProcessingException {
        gen.writeString(value.format(SecHubDateTimeFormat.FORMATTER));
    }
}