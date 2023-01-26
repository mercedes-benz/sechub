package com.mercedesbenz.sechub.domain.scan.analytic;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticProductData;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

/**
 * Importer for CLOC output (see https://github.com/AlDanial/cloc) - JSON only
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class ClocJsonAnalyticDataImporter implements AnalyticDataImporter {

    private ObjectMapper mapper;

    public ClocJsonAnalyticDataImporter() {
        JsonFactory jsonFactory = new JsonFactory();

        mapper = new ObjectMapper(jsonFactory);
    }

    @Override
    public AnalyticData importData(String clocJson) throws IOException {

        CodeAnalyticData locData = new CodeAnalyticData();

        AnalyticProductData productData = locData.getProductData();
        productData.setProductName("CLOC");

        readJsonData(clocJson, locData);

        AnalyticData data = new AnalyticData();
        data.setLinesOfCode(locData);

        return data;
    }

    private void readJsonData(String clocJson, CodeAnalyticData locData) throws JsonProcessingException, JsonMappingException {
        JsonNode rootNode = mapper.readTree(clocJson);
        Iterator<String> fieldNames = rootNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            JsonNode subNode = rootNode.get(fieldName);

            switch (fieldName.toLowerCase()) {
            case "header":
                importHeader(locData, subNode);
                break;
            case "sum":
                importSum(locData, subNode);
                break;
            default:
                importLanguage(fieldName, locData, subNode);
            }

        }
    }

    private void importHeader(CodeAnalyticData data, JsonNode subNode) {
        String version = "";
        JsonNode versionNode = subNode.get("cloc_version");
        if (versionNode != null) {
            version = versionNode.textValue();
        }
        AnalyticProductData productData = data.getProductData();
        productData.setVersion(version);

    }

    private void importLanguage(String language, CodeAnalyticData linesOfCode, JsonNode languageNode) {
        long lines = -1L;
        long files = -1L;
        JsonNode codeNode = languageNode.get("code");
        if (codeNode != null) {
            lines = codeNode.longValue();
        }
        JsonNode nFilesNode = languageNode.get("nFiles");
        if (nFilesNode != null) {
            files = nFilesNode.longValue();
        }

        linesOfCode.setLines(language, lines);
        linesOfCode.setFiles(language, files);

    }

    private void importSum(CodeAnalyticData data, JsonNode subNode) {

    }

}
