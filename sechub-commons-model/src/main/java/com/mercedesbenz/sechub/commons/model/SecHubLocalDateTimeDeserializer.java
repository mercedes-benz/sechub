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
 * This class tries to handle as much wellknown date time formats as possible
 * (to be backward compatible for older sechub data and also to read foreign
 * data) and to deserialize them into a {@link LocalDateTime} object.
 *
 * If the JSON element is a string the class tries to deserialize for the sechub
 * default time date pattern: {@value SecHubDateTimeFormat#PATTERN} otherwise
 * the default local date parsing is used. If the JSON data is array based, the
 * default jackson implementation will be used.
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubLocalDateTimeDeserializer.class);
    private StdDeserializer<LocalDateTime> jacksonDefault = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.INSTANCE;
    private static final long serialVersionUID = 1L;

    protected SecHubLocalDateTimeDeserializer() {
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