package com.mercedesbenz.sechub.commons.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMapperFactory {

    public static JsonMapper createMapper() {
        /* @formatter:off */
        JsonMapper mapper = JsonMapper.builder().
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
        javaTimeModule.addSerializer(LocalDateTime.class, new SecHubLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new SecHubLocalDateTimeDeserializer());

        mapper.registerModule(javaTimeModule); // to provide local date etc.

        return mapper;
    }
}
