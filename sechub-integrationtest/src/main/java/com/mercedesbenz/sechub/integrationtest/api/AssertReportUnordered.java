// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.JSONTestSupport;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.test.TestFileSupport;

public class AssertReportUnordered {

    private static final Logger LOG = LoggerFactory.getLogger(AssertReportUnordered.class);

    private static String lastOutputLIne;
    private JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    private JsonNode jsonObj;

    private AssertReportUnordered(String json) {
        try {
            jsonObj = jsonTestSupport.fromJson(json);
        } catch (IOException e) {
            throw new RuntimeException("Not able to read json obj", e);
        }
    }

    public static AssertReportUnordered assertReportUnordered(String json) {
        return new AssertReportUnordered(json);
    }

    public static AssertReportUnordered assertReportUnordered(ExecutionResult result) {
        lastOutputLIne = result.getLastOutputLine();
        File file = result.getJSONReportFile();
        if (!file.exists()) {
            fail("No report file found:" + file.getAbsolutePath() + "\nLast output line was:" + lastOutputLIne);
        }
        String json = TestFileSupport.loadTextFile(file, "\n");
        return assertReportUnordered(json);
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

        public AssertReportUnordered isContained() {
            check(true);
            return AssertReportUnordered.this;
        }

        public AssertReportUnordered isNotContained() {
            check(false);
            return AssertReportUnordered.this;
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

    public AssertReportUnordered hasTrafficLight(TrafficLight trafficLight) {
        JsonNode trafficLightNode = jsonObj.get("trafficLight");
        if (trafficLightNode == null) {
            dump();
            LOG.info("Last ouptput line was:" + lastOutputLIne);
            fail("No trafficlight found inside report!\nPlease look inside log for details");
        }
        String trText = trafficLightNode.asText();
        TrafficLight foundTrafficLight = TrafficLight.fromString(trText);
        if (!trafficLight.equals(foundTrafficLight)) {
            /*
             * in this case we log the complete JSON content - interesting for debugging
             */
            dump();
            LOG.info("Last ouptput line was:" + lastOutputLIne);
        }
        assertEquals("Returned traffic light:" + foundTrafficLight + " is not as expected:" + trafficLight + ". See JSON dump in log file for details.",
                trafficLight, foundTrafficLight);
        return this;
    }

    public AssertReportUnordered hasStatus(SecHubStatus expectedStatus) {
        JsonNode statusNode = jsonObj.get("status");
        if (statusNode == null) {
            dump();
            LOG.info("Last ouptput line was:" + lastOutputLIne);
            fail("No status found inside report!\nPlease look inside log for details");
        }
        SecHubStatus foundStatus = SecHubStatus.valueOf(statusNode.asText());
        assertEquals("Status not as expected!", expectedStatus, foundStatus);
        return this;
    }

    /**
     * Dumps curren json content to log output - just for debugging, do not keep it
     * inside tests
     *
     * @return assert object
     */
    public AssertReportUnordered dump() {
        LOG.info("--------------------------------------------------------------------------------------------------------");
        LOG.info("-------------------------------------------------- DUMP -----------------------------------------------");
        LOG.info("--------------------------------------------------------------------------------------------------------");
        LOG.info(jsonObj.toPrettyString());
        LOG.info("--------------------------------------------------------------------------------------------------------");
        return this;
    }

}
