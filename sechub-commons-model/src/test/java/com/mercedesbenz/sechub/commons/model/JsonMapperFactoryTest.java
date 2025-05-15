package com.mercedesbenz.sechub.commons.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonMapperFactoryTest {

    private JsonMapper mapper;

    @Test
    void factory_returns_json_mapper_with_customized_configurations() {
        /* execute */
        mapper = JsonMapperFactory.createMapper();

        /* test */
        assertThat(mapper).isNotNull();
        assertThat(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS)).isTrue();
        assertThat(mapper.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES)).isTrue();

        assertThat(mapper.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)).isTrue();
        assertThat(mapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)).isTrue();

        assertThat(mapper.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)).isFalse();
        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();

        assertThat(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)).isTrue();

        assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }
}