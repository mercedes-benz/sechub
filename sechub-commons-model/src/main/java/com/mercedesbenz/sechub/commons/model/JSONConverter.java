// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

public class JSONConverter {

    private static final JSONConverter INSTANCE = new JSONConverter();
    private static final Logger LOG = LoggerFactory.getLogger(JSONConverter.class);

    /**
     * @return shared instance
     */
    public static JSONConverter get() {
        return INSTANCE;
    }

    private JsonMapper mapper;

    public JSONConverter() {
        mapper = JsonMapperFactory.createMapper();
    }

    public String toJSON(Object object) throws JSONConverterException {
        return toJSON(object, false);
    }

    public String toJSON(Object object, boolean prettyPrinted) throws JSONConverterException {
        return toJSON(object, prettyPrinted, null);
    }

    public String toJSON(Object object, boolean prettyPrinted, JsonMapper mapper) throws JSONConverterException {
        if (object == null) {
            return "null";
        }
        if (mapper == null) {
            mapper = this.mapper;
        }
        try {
            byte[] bytes;
            if (false || prettyPrinted) {
                bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(object);
            } else {
                bytes = mapper.writeValueAsBytes(object);

            }
            return new String(bytes);
        } catch (JsonProcessingException e) {
            throw new JSONConverterException("Was not able to convert " + object.getClass().getName() + " to JSON", e);
        }
    }

    public <T> T fromJSON(Class<T> clazz, String json) throws JSONConverterException {
        if (clazz == null) {
            throw new IllegalStateException("clazz may not be null!");
        }
        /* Fall back for null values to empty string - avoid NPE */
        String string = json;
        if (json == null) {
            string = "";
        }
        try {
            byte[] bytes = string.getBytes();
            return mapper.readValue(bytes, clazz);
        } catch (IOException e) {

            LOG.debug("JSON conversion failed, origin JSON:\n{}", json);
            /*
             * we truncate json - because when JSON to big it could flood logs - debugging
             * above is only enabled sometimes, but exceptions do always accurre inside logs
             */
            String truncatedJSON = SimpleStringUtils.truncateWhenTooLong(json, 300);
            throw new JSONConverterException("Was not able to convert JSON string to " + clazz + " object\nContent was:\n" + truncatedJSON, e);
        }
    }

    public <T> List<T> fromJSONtoListOf(Class<T> clazz, String json) {
        if (clazz == null) {
            throw new IllegalStateException("clazz may not be null!");
        }
        /* Fall back for null values to empty string - avoid NPE */
        String string = json;
        if (json == null) {
            string = "";
        }
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return mapper.readValue(string, collectionType);
        } catch (JsonProcessingException e) {
            LOG.debug("JSON conversion failed, origin JSON:\n{}", json);
            /*
             * we truncate json - because when JSON to big it could flood logs - debugging
             * above is only enabled sometimes, but exceptions do always accurre inside logs
             */
            String truncatedJSON = SimpleStringUtils.truncateWhenTooLong(json, 300);
            throw new JSONConverterException("Was not able to convert JSON string to " + clazz + " object\nContent was:\n" + truncatedJSON, e);
        }
    }

}
