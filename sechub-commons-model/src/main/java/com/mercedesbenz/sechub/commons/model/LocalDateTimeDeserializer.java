// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 *
 * This deserializer class tries to be able to handle as much time date formats
 * as possible (to be backward compatible for older sechub data and also to read
 * foreign data).
 *
 * If the json element is a string the class tries to deserialize for the sechub
 * default time date pattern: {@value SecHubDateTimeFormat#PATTERN} otherwise
 * the default local date parsing mechansim is used.if possible. If time format
 * is array based, the default jackson implementation will be used.
 *
 * @author Albert Tregnaghi
 *
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalDateTimeDeserializer.class);
    private StdDeserializer<LocalDateTime> jacksonDefault = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.INSTANCE;
    private static final long serialVersionUID = 1L;

    protected LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        if (jp.isExpectedStartArrayToken()) {
            /* array, so jackson array format */
            return jacksonDefault.deserialize(jp, ctxt);
        }

        String readValueAs = jp.readValueAs(String.class);

        try {

            return LocalDateTime.parse(readValueAs, SecHubDateTimeFormat.FORMATTER);

        } catch (Exception e) {

            LOG.debug("Was not able to read from sechub default time date format - use default local date time parsing");

            try {
                return LocalDateTime.parse(readValueAs);
            } catch (Exception e2) {
                LOG.debug("Was not able to read with default local date time parsing. Use now default jackson implementation as final fallback");
                return jacksonDefault.deserialize(jp, ctxt);
            }

        }
    }

}