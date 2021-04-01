// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.test.TestFileSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class AssertSecHubReport {

    private static final Logger LOG = LoggerFactory.getLogger(AssertSecHubReport.class);

    private static String lastOutputLIne;
    private JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    private JsonNode jsonObj;

    private AssertSecHubReport(String json) {
        try {
            jsonObj = jsonTestSupport.fromJson(json);
        } catch (IOException e) {
            throw new RuntimeException("Not able to read json obj", e);
        }
    }
    
    @Deprecated // use assertReport instead (newer implementation has more details and uses common SecHubReport object inside)
    public static AssertSecHubReport assertSecHubReport(String json) {
        return new AssertSecHubReport(json);
    }

    public static AssertSecHubReport assertSecHubReport(ExecutionResult result) {
        lastOutputLIne = result.getLastOutputLine();
        File file = result.getJSONReportFile();
        if (!file.exists()) {
            fail("No report file found:" + file.getAbsolutePath() + "\nLast output line was:" + lastOutputLIne);
        }
        String json = TestFileSupport.loadTextFile(file, "\n");
        return assertSecHubReport(json);
    }

    public class AssertFinding {
        private String name;
        private String description;
        private ScanType scanType;
        private Integer id;
        private Severity severity;

        private AssertFinding() {
        }

        public AssertFinding severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public AssertFinding id(int id) {
            this.id = id;
            return this;
        }

        public AssertFinding name(String name) {
            this.name = name;
            return this;
        }

        public AssertFinding description(String description) {
            this.description = description;
            return this;
        }

        public AssertFinding scanType(ScanType scanType) {
            this.scanType = scanType;
            return this;
        }

        public AssertSecHubReport isContained() {
            check(true);
            return AssertSecHubReport.this;
        }

        public AssertSecHubReport isNotContained() {
            check(false);
            return AssertSecHubReport.this;
        }

        private void check(boolean expectedToBeFound) {
            JsonNode r = jsonObj.get("result");
            JsonNode f = r.get("findings");
            ArrayNode findings = (ArrayNode) f;
            JsonNode found = null;

            for (int i = 0; i < findings.size(); i++) {
                boolean atLeastAcceptedByOne = false;
                JsonNode finding = findings.get(i);
                /* --------------- name -------------------- */
                if (name != null) {
                    String foundName = safeText(finding.get("name"));
                    if (foundName == null) {
                        continue;
                    }
                    if (!foundName.equals(name)) {
                        continue;
                    }
                    atLeastAcceptedByOne = true;

                }

                /* --------------- id -------------------- */
                if (id != null) {
                    int foundFindingId = safeInt(finding.get("id"));
                    if (foundFindingId != id.intValue()) {
                        continue;
                    }
                    atLeastAcceptedByOne = true;

                }

                /* --------------- description ----------- */
                if (description != null) {
                    String foundDescription = safeText(finding.get("description"));
                    if (foundDescription == null) {
                        continue;
                    }
                    if (!foundDescription.equals(description)) {
                        continue;
                    }
                    atLeastAcceptedByOne = true;
                }

                /* --------------- severity ----------- */
                if (severity != null) {
                    String foundSeverity = safeText(finding.get("severity"));
                    if (foundSeverity == null) {
                        continue;
                    }
                    if (!foundSeverity.equals(severity.name())) {
                        continue;
                    }
                    atLeastAcceptedByOne = true;
                }
                /* --------------- scanType ----------- */
                if (scanType != null) {
                    String foundScanTypeId = safeText(finding.get("type"));
                    if (foundScanTypeId == null) {
                        continue;
                    }
                    if (!foundScanTypeId.equals(scanType.getId())) {
                        continue;
                    }
                    atLeastAcceptedByOne = true;
                }

                /* --------------- id -------------------- */
                if (atLeastAcceptedByOne) {
                    found = finding;
                    break;
                }
            }
            if (found == null && expectedToBeFound) {
                fail("Not found finding:" + this + "\n\nSource:" + jsonObj.toPrettyString());
            } else if (found != null && !expectedToBeFound) {
                fail("Did found entry:" + found.toPrettyString() + ", by searching for :" + this + "\n\nSource:" + jsonObj.toPrettyString());
            }
        }

        private String safeText(JsonNode node) {
            if (node == null) {
                return null;
            }
            return node.asText();
        }

        private int safeInt(JsonNode node) {
            if (node == null) {
                return -1;
            }
            return node.asInt();
        }

        @Override
        public String toString() {
            return "AssertFinding [" + (name != null ? "name=" + name + ", " : "") + (description != null ? "description=" + description + ", " : "")
                    + (scanType != null ? "scanType=" + scanType + ", " : "") + (id != null ? "id=" + id + ", " : "")
                    + (severity != null ? "severity=" + severity : "") + "]";
        }
    }

    public AssertFinding finding() {
        return new AssertFinding();
    }

    public AssertSecHubReport hasTrafficLight(TrafficLight trafficLight) {
        JsonNode r = jsonObj.get("trafficLight");
        if (r == null) {
            fail("No trafficlight found inside report!\nLast output line was:" + lastOutputLIne);
        }
        String trText = r.asText();
        assertEquals(trafficLight, TrafficLight.fromString(trText));
        return this;
    }

    /**
     * Dumps curren json content to log output - just for debugging, do not keep it
     * inside tests
     * 
     * @return assert object
     */
    public AssertSecHubReport dump() {
        LOG.info("--------------------------------------------------------------------------------------------------------");
        LOG.info("-------------------------------------------------- DUMP -----------------------------------------------");
        LOG.info("--------------------------------------------------------------------------------------------------------");
        LOG.info(jsonObj.toPrettyString());
        LOG.info("--------------------------------------------------------------------------------------------------------");
        return this;
    }

}
