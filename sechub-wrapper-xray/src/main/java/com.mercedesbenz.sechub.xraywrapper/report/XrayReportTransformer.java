package com.mercedesbenz.sechub.xraywrapper.report;

import static java.lang.Float.parseFloat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;

public class XrayReportTransformer {

    public JsonNode getRootDataNode(File xraySecurityReport) throws XrayWrapperReportException {
        try {
            return new ObjectMapper().readTree(xraySecurityReport).get("data");
        } catch (IOException e) {
            throw new XrayWrapperReportException("Error: could not read file as json", e, XrayWrapperExitCode.IO_ERROR);
        }
    }

    public HashMap<String, XrayCycloneVulnerability> transfromSecurityReport(JsonNode rootDataNode) throws XrayWrapperReportException {
        HashMap<String, XrayCycloneVulnerability> vulnerabilityHashMap = new HashMap<String, XrayCycloneVulnerability>();
        for (JsonNode node : rootDataNode) {
            XrayCycloneVulnerability vulnerability = new XrayCycloneVulnerability("");
            setBomRef(node, vulnerability);
            String cvssString = null;
            try {
                cvssString = getAndSetCveDetails(node, vulnerability);
            } catch (JsonProcessingException e) {
                throw new XrayWrapperReportException("Error: could not process json", e, XrayWrapperExitCode.JSON_NOT_PROCESSABLE);
            }
            setSource(node, vulnerability);
            if (!cvssString.isEmpty()) {
                setRatingFromString(node, vulnerability, cvssString);
            }
            setDescription(node, vulnerability);
            addAffects(node, vulnerability);
            vulnerabilityHashMap.put(vulnerability.getId(), vulnerability);
        }
        return vulnerabilityHashMap;
    }

    private void setBomRef(JsonNode node, XrayCycloneVulnerability vulnerability) {
        String bomRef = node.get("id").asText();
        if (bomRef != null) {
            // set bomRef as ID if vulnerability does not have CVE ID
            // cycloneDX vulnerabilities also have XRAY ID as ID (mapping works)
            vulnerability.setBom_ref(bomRef);
            vulnerability.setId(bomRef);
        }
    }

    private String getAndSetCveDetails(JsonNode node, XrayCycloneVulnerability vulnerability) throws JsonProcessingException {
        final JsonNode cveDetailsNode = new ObjectMapper().readTree(String.valueOf(node)).get("component_versions").get("more_details").get("cves");
        String cvssString = "";
        JsonNode cvssNode;
        JsonNode cveIDNode;
        for (JsonNode cveDetail : cveDetailsNode) {
            cvssNode = cveDetail.get("cvss_v3");
            cveIDNode = cveDetail.get("cve");
            ArrayNode cwe = (ArrayNode) cveDetail.get("cwe");
            if (cvssNode != null) {
                cvssString = cvssNode.asText();
            }
            if (cveIDNode != null) {
                vulnerability.setId(cveIDNode.asText());
            }
            if (cwe != null) {
                vulnerability.setCwe(cwe);
            }
        }
        return cvssString;
    }

    private void setSource(JsonNode node, XrayCycloneVulnerability vulnerability) {
        String name = node.get("provider").asText();
        String url = "";
        if (name != null) {
            vulnerability.setSource(url, name);
        }
    }

    private void setRatingFromString(JsonNode node, XrayCycloneVulnerability vulnerability, String cvssString) {
        String[] cvssArray = cvssString.split("/", 3);
        String score = cvssArray[0];
        Float scoreFloat = parseFloat(score);
        String method = cvssArray[1];
        String vector = cvssArray[2];
        method = method.replace(":", "v");
        method = method.replace(".", "");
        method = method.replace("0", "");
        String severity = node.get("severity").asText().toLowerCase();
        String severitySource = node.get("severity_source").asText().toUpperCase();
        if (severity != null && severitySource != null && method != null && vector != null && score != null) {
            vulnerability.setRating(scoreFloat, severity, method, vector, severitySource);
        }
    }

    private void setDescription(JsonNode node, XrayCycloneVulnerability vulnerability) {
        String description = node.get("component_versions").get("more_details").get("description").asText();
        if (description != null) {
            vulnerability.setDescription(description);
        }
    }

    private void addAffects(JsonNode node, XrayCycloneVulnerability vulnerability) {
        String ref = node.get("source_comp_id").asText();
        String purls = node.get("source_id").asText();
        ArrayNode vulnerableVersions = (ArrayNode) node.get("component_versions").get("vulnerable_versions");
        ArrayNode fixedVersions = (ArrayNode) node.get("component_versions").get("fixed_versions");
        vulnerability.addAffects(ref, vulnerableVersions, fixedVersions, purls);
        vulnerability.putAffects();
    }
}
