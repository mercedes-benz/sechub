package com.mercedesbenz.sechub.domain.scan.analytic;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticProductInfo;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

/**
 * Importer for CLOC output (see https://github.com/AlDanial/cloc) - JSON only
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class ClocJsonAnalyticDataImporter implements AnalyticDataPartImporter<CodeAnalyticData> {

    private ObjectMapper mapper;

    public ClocJsonAnalyticDataImporter() {
        JsonFactory jsonFactory = new JsonFactory();

        mapper = new ObjectMapper(jsonFactory);
    }

    @Override
    public boolean isAbleToImport(String analyticDataAsString) {
        if (analyticDataAsString == null) {
            return false;
        }

        boolean couldBeJson = analyticDataAsString.contains("{");
        boolean isCloc = analyticDataAsString.contains("\"cloc_version\"");

        return couldBeJson && isCloc;
    }

    @Override
    public CodeAnalyticData importData(String clocJson) throws IOException {

        CodeAnalyticData data = new CodeAnalyticData();

        AnalyticProductInfo productData = data.getProductInfo();
        productData.setName("CLOC");

        readJsonData(clocJson, data);

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
                /* we do nothing here - sum is calculated automatically by analytic data */
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
        AnalyticProductInfo productData = data.getProductInfo();
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

        linesOfCode.setLinesOfCodeForLanguage(language, lines);
        linesOfCode.setFilesForLanguage(language, files);

    }

}
