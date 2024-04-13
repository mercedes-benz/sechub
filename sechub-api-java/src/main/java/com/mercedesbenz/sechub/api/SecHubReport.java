// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;

public class SecHubReport extends com.mercedesbenz.sechub.commons.model.SecHubReportModel {

    private static JsonMapper mapper = JsonMapperFactory.createMapper();

    public static SecHubReport fromFile(File file) throws SecHubReportException {
        SecHubReport report = null;

        try {
            report = mapper.readValue(file, SecHubReport.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new SecHubReportException("Content is not valid JSON", e);
        } catch (IOException e) {
            throw new SecHubReportException("Wasn't able to read report file", e);
        }

        return report;
    }
}
