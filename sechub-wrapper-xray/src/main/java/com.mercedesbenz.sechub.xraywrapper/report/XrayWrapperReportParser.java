package com.mercedesbenz.sechub.xraywrapper.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static java.lang.Double.parseDouble;

/**
 * parses the xray security report vulnerabilities to cycloneDX vulnerabilities
 */

public class XrayWrapperReportParser {
    public JsonNode getRootDataNode(File xraySecurityReport) throws XrayWrapperReportException {
        try {
            return new ObjectMapper().readTree(xraySecurityReport).get("data");
        } catch (IOException e) {
            throw new XrayWrapperReportException("Could not read file as json", e, XrayWrapperExitCode.IO_ERROR);
        }
    }

    public HashMap<String, CycloneDXVulnerabilityHelper> transformSecurityReport(JsonNode rootDataNode) throws XrayWrapperReportException {
        HashMap<String, CycloneDXVulnerabilityHelper> vulnerabilityHashMap = new HashMap<>();

        for (JsonNode node : rootDataNode) {
            CycloneDXVulnerabilityHelper vulnerability = new CycloneDXVulnerabilityHelper();
            setBomRef(node, vulnerability);
            String cvssString;
            try {
                cvssString = getAndSetCveDetails(node, vulnerability);
            } catch (JsonProcessingException e) {
                throw new XrayWrapperReportException("Could not process json", e, XrayWrapperExitCode.JSON_NOT_PROCESSABLE);
            }
            setSource(node, vulnerability);
            if (!cvssString.isEmpty()) {
                setRatingFromString(node, vulnerability, cvssString);
            }
            setDescription(node, vulnerability);
            addAffects(node, vulnerability);
            vulnerabilityHashMap.put(vulnerability.getVulnerability().getId(), vulnerability);
        }
        return vulnerabilityHashMap;
    }

    private void setBomRef(JsonNode node, CycloneDXVulnerabilityHelper vulnerability) {
        String bomRef = node.get("id").asText();
        if (bomRef != null) {
            // set bomRef as ID if vulnerability does not have CVE ID
            // cycloneDX vulnerabilities also have XRAY ID as ID (mapping works)
            vulnerability.getVulnerability().setBomRef(bomRef);
            vulnerability.getVulnerability().setId(bomRef);
        }
    }

    private String getAndSetCveDetails(JsonNode node, CycloneDXVulnerabilityHelper vulnerability) throws JsonProcessingException {
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
                vulnerability.getVulnerability().setId(cveIDNode.asText());
            }
            if (cwe != null) {
                vulnerability.addCWE(cwe);
            }
        }
        return cvssString;
    }

    private void setSource(JsonNode node, CycloneDXVulnerabilityHelper vulnerability) {
        String name = node.get("provider").asText();
        String url = "";
        if (name != null) {
            vulnerability.addSource(url, name);
        }
    }

    private void setRatingFromString(JsonNode node, CycloneDXVulnerabilityHelper vulnerability, String cvssString) {
        String[] cvssArray = cvssString.split("/", 3);
        String score = cvssArray[0];
        Double scoreDouble = parseDouble(score);
        String method = cvssArray[1];
        String vector = cvssArray[2];
        method = method.replace(":", "v");
        method = method.replace(".", "");
        method = method.replace("0", "");
        String severity = node.get("severity").asText().toLowerCase();
        String severitySource = node.get("severity_source").asText().toUpperCase();
        if (severity != null && severitySource != null && method != null && vector != null && score != null) {
            vulnerability.addRating(scoreDouble, severity, method, vector, severitySource);
        }
    }

    private void setDescription(JsonNode node, CycloneDXVulnerabilityHelper vulnerability) {
        String description = node.get("component_versions").get("more_details").get("description").asText();
        if (description != null) {
            vulnerability.getVulnerability().setDescription(description);
        }
    }

    private void addAffects(JsonNode node, CycloneDXVulnerabilityHelper vulnerability) {
        String ref = node.get("source_comp_id").asText();
        vulnerability.getVulnerability().setBomRef(ref);
        ArrayNode vulnerableVersions = (ArrayNode) node.get("component_versions").get("vulnerable_versions");
        ArrayNode fixedVersions = (ArrayNode) node.get("component_versions").get("fixed_versions");
        vulnerability.addAffects(ref, vulnerableVersions, fixedVersions);
    }

}
