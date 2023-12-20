// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayWrapperJSONConverter {

    private static final XrayWrapperJSONConverter INSTANCE = new XrayWrapperJSONConverter();

    public static XrayWrapperJSONConverter get() {
        return INSTANCE;
    }

    private ObjectMapper mapper;

    public XrayWrapperJSONConverter() {
        mapper = new ObjectMapper();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public JsonNode readJSONFromString(String jsonString) throws XrayWrapperException {
        try {
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new XrayWrapperException("Cannot parse provided string into JSON", XrayWrapperExitCode.INVALID_JSON, e);
        }
    }

    public JsonNode readJSONFromFile(File file) throws XrayWrapperException {
        try {
            return mapper.readTree(file);
        } catch (JsonProcessingException e) {
            throw new XrayWrapperException("Cannot parse provided string into JSON", XrayWrapperExitCode.INVALID_JSON, e);
        } catch (IOException e) {
            throw new XrayWrapperException("Cannot read JSON from file", XrayWrapperExitCode.IO_ERROR, e);
        }
    }

}
