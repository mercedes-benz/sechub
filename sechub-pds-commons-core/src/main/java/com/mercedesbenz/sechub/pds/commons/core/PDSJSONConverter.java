// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;

public class PDSJSONConverter {

    private static final PDSJSONConverter INSTANCE = new PDSJSONConverter();
    private static final Logger LOG = LoggerFactory.getLogger(PDSJSONConverter.class);

    /**
     * @return shared instance
     */
    public static PDSJSONConverter get() {
        return INSTANCE;
    }

    private ObjectMapper mapper;

    public PDSJSONConverter() {
        mapper = JsonMapperFactory.createMapper();
    }

    public String toJSON(Object object) throws PDSJSONConverterException {
        return toJSON(object, false);
    }

    public String toJSON(Object object, boolean prettyPrinted) throws PDSJSONConverterException {
        if (object == null) {
            return "null";
        }
        try {
            byte[] bytes;
            if (prettyPrinted) {
                bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(object);
            } else {
                bytes = mapper.writeValueAsBytes(object);

            }
            return new String(bytes);
        } catch (JsonProcessingException e) {
            throw new PDSJSONConverterException("Was not able to convert " + object.getClass().getName() + " to JSON", e);
        }
    }

    public <T> T fromJSON(Class<T> clazz, String jSON) throws PDSJSONConverterException {
        if (clazz == null) {
            throw new IllegalStateException("clazz may not be null!");
        }
        /* Fall back for null values to empty string - avoid NPE */
        String string = jSON;
        if (jSON == null) {
            string = "";
        }
        try {
            byte[] bytes = string.getBytes();
            return mapper.readValue(bytes, clazz);
        } catch (IOException e) {
            LOG.debug("JSON conversion failed:\n{}", jSON);
            throw new PDSJSONConverterException("Was not able to convert JSON string to " + clazz + " object", e);
        }
    }

}
