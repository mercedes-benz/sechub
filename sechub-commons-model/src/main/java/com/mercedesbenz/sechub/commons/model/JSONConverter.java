// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
        /* @formatter:off */
        mapper = JsonMapper.builder().
            enable(JsonParser.Feature.ALLOW_COMMENTS).
            enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES).
            /*
             * next line will write single element array as simple strings. There was an
             * issue with this when serializing/deserializing SimpleMailMessage class from
             * spring when only one "to" defined but was an array - jackson had problems see
             * also: https://github.com/FasterXML/jackson-databind/issues/720 and
             * https://stackoverflow.com/questions/39041496/how-to-enforce-accept-single-
             * value-as-array-in-jacksons-deserialization-process
             */
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).
            disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED).

            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).
            /*
             * we accept enums also case insensitive - e.g Traffic light shall be accesible
             * by "GREEN" but also "green"...
             */
            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).

        build();
        /* @formatter:on */

        // remove absent parts from Json, so it is more "compact" / without boiler plate
        // code
        mapper.setSerializationInclusion(Include.NON_ABSENT);

        mapper.registerModule(new Jdk8Module()); // to provide optional etc.

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'")));

        mapper.registerModule(javaTimeModule); // to provide local date etc.

    }

    public String toJSON(Object object) throws JSONConverterException {
        return toJSON(object, false);
    }

    public String toJSON(Object object, boolean prettyPrinted) throws JSONConverterException {
        if (object == null) {
            return "null";
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
