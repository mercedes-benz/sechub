// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import static com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportConstants.*;
import static java.lang.Double.parseDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cyclonedx.model.vulnerability.Vulnerability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * TTransforms the xray security report vulnerabilities and to cycloneDX
 * vulnerabilities
 */
public class XrayWrapperReportTransformer {
    private record RatingRecord(double scoreDouble, String severity, String method, String vector, String severitySource) {
    }

    public Map<String, Vulnerability> transformVulnerabilitiesFromSecurityReport(JsonNode xraySecurityReportRootNode) throws XrayWrapperReportException {
        if (xraySecurityReportRootNode == null) {
            throw new XrayWrapperReportException("Xray Security Report node was NULL");
        }
        Map<String, Vulnerability> vulnerabilityHashMap = new HashMap<>();

        for (JsonNode securityReportVulnerabilityNode : xraySecurityReportRootNode) {

            Vulnerability vulnerability = new Vulnerability();

            addBomRefToVulnerability(securityReportVulnerabilityNode, vulnerability);
            extractCVEDetails(securityReportVulnerabilityNode, vulnerability);
            extractSourceInformation(securityReportVulnerabilityNode, vulnerability);
            addDescriptionToVulnerability(securityReportVulnerabilityNode, vulnerability);
            extractComponentVersions(securityReportVulnerabilityNode, vulnerability);

            vulnerabilityHashMap.put(vulnerability.getId(), vulnerability);
        }
        return vulnerabilityHashMap;
    }

    private void addBomRefToVulnerability(JsonNode node, Vulnerability vulnerability) {
        String bomRef = node.get(ID).asText();
        if (bomRef != null) {
            // set bomRef as ID if vulnerability does not have CVE ID
            // cycloneDX vulnerabilities also have XRAY ID as ID (mapping works)
            vulnerability.setBomRef(bomRef);
            vulnerability.setId(bomRef);
        }
    }

    private void extractCVEDetails(JsonNode securityReportVulnerabilityNode, Vulnerability vulnerability) {
        JsonNode cveDetailsNode = securityReportVulnerabilityNode.get(COMPONENT_VERSION).get(MORE_DETAILS).get(CVES);

        for (JsonNode cveDetail : cveDetailsNode) {
            JsonNode cveIdNode = cveDetail.get(CVE);
            if (cveIdNode != null) {
                // ID can be empty when vulnerability has only Jfrog ID
                vulnerability.setId(cveIdNode.asText());
            }

            ArrayNode cwe = (ArrayNode) cveDetail.get(CWE);
            if (cwe != null) {
                // CWE can be empty
                addCWEToVulnerability(cwe, vulnerability);
            }

            // CVSS can be empty and different versions are available
            // current standard is V3
            JsonNode cvssNode_v3 = cveDetail.get(CVSS_V3);
            JsonNode cvssNode_v2 = cveDetail.get(CVSS_V2);
            JsonNode cvssNode_v4 = cveDetail.get(CVSS_V4);
            String cvssString = null;

            if (cvssNode_v3 != null) {
                cvssString = cvssNode_v3.asText();
            } else if (cvssNode_v2 != null) {
                cvssString = cvssNode_v2.asText();
            } else if (cvssNode_v4 != null) {
                cvssString = cvssNode_v4.asText();
            }
            if (cvssString != null) {
                extractRatingFromCVSSString(securityReportVulnerabilityNode, vulnerability, cvssString);
            }
        }
    }

    private void addCWEToVulnerability(ArrayNode cweArray, Vulnerability vulnerability) {
        List<Integer> cwes = new ArrayList<>();

        for (JsonNode node : cweArray) {
            String cweInfo = node.asText();
            if (cweInfo.contains("noinfo")) {
                // no information about cwe available
                continue;
            }
            if (cweInfo.isEmpty()) {
                continue;
            }
            try {
                cweInfo = cweInfo.split("-")[1];
                cwes.add(Integer.parseInt(cweInfo));
            } catch (NumberFormatException e) {
                // if anything else is in the cwe info that does not contain cwe number we don't
                // want to add it, cycloneDX only allows integer, but cwes are optional
            }
        }
        vulnerability.setCwes(cwes);
    }

    private void extractSourceInformation(JsonNode node, Vulnerability vulnerability) {
        String name = node.get(PROVIDER).asText();
        String url = "";
        if (name != null) {
            addSourceToVulnerability(url, name, vulnerability);
        }
    }

    private void addSourceToVulnerability(String sourceUrl, String sourceName, Vulnerability vulnerability) {
        Vulnerability.Source source = new Vulnerability.Source();
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        vulnerability.setSource(source);
    }

    private void extractRatingFromCVSSString(JsonNode node, Vulnerability vulnerability, String cvssString) {
        // cvssString from security report contains multiple information fragments
        // (score, method and vector)
        // example: "cvss_v3": "9.8/CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H"
        String[] cvssArray = cvssString.split("/", 3);

        String score = cvssArray[0];
        double scoreDouble = parseDouble(score);
        String method = cvssArray[1];
        String vector = cvssArray[2];
        method = method.replace(":", "v");
        method = method.replace(".", "");
        method = method.replace("0", "");
        String severity = node.get(SEVERITY).asText().toLowerCase();
        String severitySource = node.get(SEVERITY_SOURCE).asText().toUpperCase();

        if (severity != null && severitySource != null && method != null && vector != null && score != null) {
            RatingRecord ratingRecord = new RatingRecord(scoreDouble, severity, method, vector, severitySource);
            addRatingToVulnerability(ratingRecord, vulnerability);
        }
    }

    private void addRatingToVulnerability(RatingRecord ratingRecord, Vulnerability vulnerability) {
        Vulnerability.Rating rating = new Vulnerability.Rating();

        String sourceUrl = "";
        String id = vulnerability.getId();
        if (!id.contains("XRAY")) {
            sourceUrl = NVD_URL + id;
        }

        Vulnerability.Source source = new Vulnerability.Source();
        source.setName(ratingRecord.severitySource());
        source.setUrl(sourceUrl);
        rating.setSource(source);

        rating.setScore(ratingRecord.scoreDouble());

        rating.setSeverity(Vulnerability.Rating.Severity.fromString(ratingRecord.severity()));

        rating.setMethod(Vulnerability.Rating.Method.fromString(ratingRecord.method()));

        rating.setVector(ratingRecord.vector());

        vulnerability.addRating(rating);
    }

    private void addDescriptionToVulnerability(JsonNode node, Vulnerability vulnerability) {
        String description = node.get(COMPONENT_VERSION).get(MORE_DETAILS).get(DESCRIPTION).asText();
        if (description != null) {
            vulnerability.setDescription(description);
        }
    }

    private void extractComponentVersions(JsonNode node, Vulnerability vulnerability) {
        String ref = node.get(SOURCE_COMP_ID).asText();
        vulnerability.setBomRef(ref);
        ArrayNode vulnerableVersions = (ArrayNode) node.get(COMPONENT_VERSION).get(VULNERABLE_VERSION);
        ArrayNode fixedVersions = (ArrayNode) node.get(COMPONENT_VERSION).get(FIXED_VERSION);
        addComponentVersionsToVulnerability(vulnerability, ref, vulnerableVersions, fixedVersions);
    }

    public void addComponentVersionsToVulnerability(Vulnerability vulnerability, String reference, ArrayNode vulnerableVersions, ArrayNode fixedVersions) {
        Vulnerability.Affect affect = new Vulnerability.Affect();
        List<Vulnerability.Affect> affects = new ArrayList<>();
        List<Vulnerability.Version> versions = new ArrayList<>();

        affect.setRef(reference);
        String packageName = reference.split(":")[0];

        transformVersions(vulnerableVersions, "affected", packageName, versions);
        transformVersions(fixedVersions, "unaffected", packageName, versions);

        affect.setVersions(versions);
        affects.add(affect);
        vulnerability.setAffects(affects);
    }

    private void transformVersions(ArrayNode securityVersions, String status, String packageName, List<Vulnerability.Version> versions) {
        // "All Versions", "< 1.19.10", "1.20.0-0 ≤ Version < 1.20.5", "1.2.34"
        // cycloneDX using Version Range Spec (vers) vers:npm/1.2.3|>=2.0.0|<5.0.0
        packageName = "vers:" + packageName + "/";

        if (securityVersions != null) {
            for (JsonNode entry : securityVersions) {

                String versionString = entry.asText();
                Vulnerability.Version version = new Vulnerability.Version();

                if (versionString.equals("All Versions")) {
                    version.setRange(packageName + ">=0.0.0");

                } else if (versionString.contains("Version")) {
                    versionString = versionString.replace(" ", "");
                    String[] split = versionString.split("Version");
                    version.setRange(packageName + split[0] + "|" + split[1]);

                } else if (versionString.contains("<") | versionString.contains("≤")) {
                    versionString = versionString.replace(" ", "");
                    version.setRange(packageName + versionString);

                } else {
                    version.setVersion(versionString);
                }

                version.setStatus(Vulnerability.Version.Status.fromString(status));
                versions.add(version);
            }
        }
    }
}
