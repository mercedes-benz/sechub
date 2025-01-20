// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class TestSystemExampleWriter {

    private static final Logger LOG = LoggerFactory.getLogger(TestSystemExampleWriter.class);

    public static void writeExample(SystemTestConfiguration configuration, String name) throws IOException {
        String configurationAsPrettyPrintedJson = createSmallPrettyPrintedJson(configuration);

        TextFileWriter writer = new TextFileWriter();
        File generatedSecHubDocExampleFile = new File("./build/gen/example/" + name);
        writer.writeTextToFile(generatedSecHubDocExampleFile, configurationAsPrettyPrintedJson, true);

        LOG.info("Wrote configuration data as example doc file into: {}", generatedSecHubDocExampleFile.getAbsolutePath());
    }

    private static String createSmallPrettyPrintedJson(SystemTestConfiguration configuration) {
        JsonMapper mapper = JsonMapperFactory.createMapper();
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        String configurationAsPrettyPrintedJson = JSONConverter.get().toJSON(configuration, true, mapper);
        return configurationAsPrettyPrintedJson;
    }

}
