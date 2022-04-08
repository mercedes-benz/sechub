// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;

public class TestJSONHelper {

    private static final TestJSONHelper INSTANCE = new TestJSONHelper();

    private static final Logger LOG = LoggerFactory.getLogger(TestJSONHelper.class);

    /**
     * @return shared instance
     */
    public static TestJSONHelper get() {
        return INSTANCE;
    }

    private ObjectMapper mapper;

    public ObjectMapper getMapper() {
        return mapper;
    }

    public TestJSONHelper() {
        // https://github.com/FasterXML/jackson-core/wiki/JsonParser-Features
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

        mapper = new ObjectMapper(jsonFactory);
        /*
         * next line will write single element array as simple strings. There was an
         * issue with this when serializing/deserializing SimpleMailMessage class from
         * spring when only one "to" defined but was an array - jackson had problems see
         * also: https://github.com/FasterXML/jackson-databind/issues/720 and
         * https://stackoverflow.com/questions/39041496/how-to-enforce-accept-single-
         * value-as-array-in-jacksons-deserialization-process
         */
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        // but we do NOT use SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED !
        // reason: otherwise jackson does all single ones write as not being an array
        // which comes up to problems agani
        mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);

        // http://www.baeldung.com/jackson-ignore-null-fields
        mapper.setSerializationInclusion(Include.NON_NULL);
        // http://www.baeldung.com/jackson-optional
        mapper.registerModule(new Jdk8Module());
    }

    public void assertValidJson(String string) {
        try {
            readTree(string);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("This is not valid json:\n" + string, e);
        }
    }

    public JsonNode readTree(String string) {
        JsonNode node;
        try {
            node = mapper.reader().readTree(string);
        } catch (IOException e) {
            throw new IllegalStateException("Did not expect IO problems", e);
        }
        return node;
    }

    public String beatuifyJSON(String json) {
        if (json == null) {
            return null;
        }
        try {
            Object jsonObj = mapper.readValue(json, Object.class);
            String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
            return indented;
        } catch (IOException e) {
            LOG.error("Was not able to beautify json, will return origin text as fallback");
            return json;
        }
    }

    public <T> T createFromJSON(String json, Class<T> clazz) {
        try {
            return getMapper().readValue(json.getBytes(), clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot convert given JSON to clazz:" + clazz, e);
        }
    }

    public <T> MappingIterator<T> createValuesFromJSON(String json, Class<T> clazz) {
        try {
            return getMapper().readerFor(clazz).readValues(json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot convert given JSON to clazz:" + clazz, e);
        }
    }

    public String createJSON(Object object, boolean prettyPrinted) throws JSONConverterException {
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
            throw new JSONConverterException("Was not able to convert " + object.getClass().getName() + " to JSON", e);
        }
    }

    public String createJSON(Object object) {
        return createJSON(object, false);
    }

}
