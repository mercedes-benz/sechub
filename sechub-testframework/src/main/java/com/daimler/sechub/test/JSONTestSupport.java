// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONTestSupport {

	public static final JSONTestSupport DEFAULT = new JSONTestSupport();
	
	private ObjectMapper objectMapper;

	public JSONTestSupport(){
		objectMapper=createObjectMapper();
	}
	
	/**
	 * Creates jackson object mapper, can be overriden
	 * @return object mapper
	 */
	protected ObjectMapper createObjectMapper() {
		return new ObjectMapper();
	}

	public String toJson(Map<String, ?> json) throws IOException {
		return objectMapper.writeValueAsString(json);
	}
	
	public JsonNode fromJson(String json) throws IOException {
		return objectMapper.readTree(json);
	}
}