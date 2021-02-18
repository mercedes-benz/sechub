// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JSONDeveloperHelper {

	private static final Logger LOG = LoggerFactory.getLogger(JSONDeveloperHelper.class);

	public static final JSONDeveloperHelper INSTANCE = new JSONDeveloperHelper();

	private ObjectMapper mapper;

	public JSONDeveloperHelper() {
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

	public ObjectMapper getMapper() {
        return mapper;
    }
	
	public String beatuifyJSON(String json) {
		if (json==null) {
			return null;
		}
		try {
			Object jsonObj = mapper.readValue(json, Object.class);
			String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
			return indented;
		}catch(IOException e) {
			LOG.error("Was not able to beautify json, will return origin text as fallback");
			return json;
		}
	}

    public String compress(String json) {
        if (json==null) {
            return null;
        }
        try {
            Object jsonObj = mapper.readValue(json, Object.class);
            String notIndented = mapper.writer().writeValueAsString(jsonObj);
            return notIndented;
        }catch(IOException e) {
            LOG.error("Was not able to compress json, will return origin text as fallback");
            return json;
        }
        
    }
}
