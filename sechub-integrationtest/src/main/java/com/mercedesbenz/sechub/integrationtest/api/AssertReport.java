// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubReportData;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubReportVersion;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubJobAutoDumper;

public class AssertReport {

    private static final Logger LOG = LoggerFactory.getLogger(AssertReport.class);

    private SecHubReportModel report;

    private SecHubJobAutoDumper autoDumper = new SecHubJobAutoDumper();

    public static AssertReport assertReport(String json) {
        return new AssertReport(SecHubReportModel.fromJSONString(json));
    }

    AssertReport(SecHubReportModel report) {
        assertNotNull("Report may not be null", report);
        this.report = report;
    }

    public AssertReport enablePDSAutoDumpOnErrorsForSecHubJob(UUID sechubJobUUID) {
        this.autoDumper.enablePDSAutoDumpOnErrorsForSecHubJob();
        this.autoDumper.setSecHubJobUUID(sechubJobUUID);
        return this;
    }

    public AssertReport hasFindings(int expectedCount) {
        List<SecHubFinding> findings = assertFindings(report);
        autoDumper.execute(() -> assertEquals(expectedCount, findings.size()));
        return this;
    }

    public AssertReportUnordered hasUnordered() {
        return AssertReportUnordered.assertReportUnordered(report.toJSON());
    }

    public AssertReport hasMessages(int expectedAmountOfMessages) {
        autoDumper.execute(() -> assertEquals(expectedAmountOfMessages, report.getMessages().size()));
        return this;
    }

    public AssertReport hasMessage(SecHubMessageType type, String message) {
        autoDumper.execute(() -> {
            SecHubMessage expectedMessage = new SecHubMessage(type, message);

            if (!report.getMessages().contains(expectedMessage)) {

                StringBuilder sb = new StringBuilder();
                sb.append("Did not found message:\n-").append(expectedMessage);
                sb.append("\nFollowing messages found:\n");
                for (SecHubMessage sechubMessage : report.getMessages()) {
                    sb.append("-");
                    sb.append(sechubMessage.toString());
                    sb.append("\n");
                }
                fail(sb.toString());
            }
        });
        return this;
    }

    public AssertReport hasStatus(SecHubStatus expectedStatus) {
        if (!Objects.equals(expectedStatus, report.getStatus())) {
            dump();
            autoDumper.execute(() -> assertEquals(expectedStatus, report.getStatus()));
        }
        return this;
    }

    public AssertReport hasTrafficLight(TrafficLight expectedLight) {
        autoDumper.execute(() -> assertEquals(expectedLight, report.getTrafficLight()));
        return this;
    }

    public AssertReport hasReportVersion(SecHubReportVersion version) {
        assertNotNull("Wrong implemented unit test, given version may not be null!", version);
        autoDumper.execute(() -> assertEquals(version.getVersionAsString(), report.getReportVersion()));
        return this;
    }

    public AssertFinding finding(int number) {
        SecHubFinding secHubFinding = assertFindings(report).get(number);
        return new AssertFinding(secHubFinding, number);
    }

    public class AssertFinding {

        private SecHubFinding finding;

        public AssertFinding(SecHubFinding finding, int number) {
            autoDumper.execute(() -> assertNotNull("Finding may not be null! But was for number:" + number, finding));
            this.finding = finding;
        }

        public AssertReport andReport() {
            return AssertReport.this;
        }

        public AssertFinding hasId(int id) {
            autoDumper.execute(() -> assertEquals(id, finding.getId()));
            return this;
        }

        public AssertFinding hasNotId(int id) {
            autoDumper.execute(() -> assertNotEquals(id, finding.getId()));
            return this;
        }

        public AssertFinding hasName(String name) {
            autoDumper.execute(() -> assertEquals(name, finding.getName()));
            return this;
        }

        public AssertFinding hasScanType(ScanType type) {
            autoDumper.execute(() -> assertEquals(type, finding.getType()));
            return this;
        }

        public AssertFinding hasNotScanType(ScanType type) {
            autoDumper.execute(() -> assertNotEquals(type, finding.getType()));
            return this;
        }

        public String getDescription() {
            return finding.getDescription();
        }

        public AssertFinding hasDescription(String description) {
            autoDumper.execute(() -> assertEquals(description, finding.getDescription()));
            return this;
        }

        public AssertFinding hasDescriptionContaining(String descriptionPart) {
            if (!finding.getDescription().contains(descriptionPart)) {
                dump();
                // we use assertEquals here, so in IDEs we got a compare function (e.g. eclipse
                // double click on first stacktrace element (ComparisionFailure))
                autoDumper.execute(
                        () -> assertEquals("The description part '" + descriptionPart + "' is not inside finding!", descriptionPart, finding.getDescription()));
            }
            return this;
        }

        public AssertFinding hasNoHostnames() {
            return hasHostnames();
        }

        public AssertFinding hasHostnames(String... hostnames) {
            List<String> hostnames2 = finding.getHostnames();

            for (String hostname : hostnames) {
                if (!hostnames2.contains(hostname)) {
                    autoDumper.execute(() -> fail("Hostname:" + hostname + " not found in finding!"));
                }
            }
            autoDumper.execute(() -> assertEquals(hostnames.length, hostnames2.size()));
            return this;
        }

        public AssertFinding finding(int number) {
            return AssertReport.this.finding(number);
        }

        public AssertFinding hasNoReferences() {
            return hasReferences();
        }

        public AssertFinding hasReferences(String... references) {
            List<String> references2 = finding.getReferences();

            for (String reference : references) {
                if (!references2.contains(reference)) {
                    autoDumper.execute(() -> fail("Reference:" + reference + " not found in finding!"));
                }
            }
            autoDumper.execute(() -> assertEquals(references.length, references2.size()));
            return this;
        }

        public AssertFinding hasSeverity(Severity severity) {
            if (!Objects.equals(severity, finding.getSeverity())) {
                dump();
                autoDumper.execute(() -> assertEquals("Finding id:" + finding.getId() + " has not expected severity!", severity, finding.getSeverity()));
            }
            return this;
        }

        public AssertFinding hasCweId(int cweId) {
            if (finding.getCweId() == null) {
                dump();
                autoDumper.execute(() -> fail("No cwe id found inside finding at all!"));
            }
            autoDumper.execute(() -> assertEquals("CWE id not as expected", cweId, finding.getCweId().intValue()));
            return this;
        }

        public AssertFinding hasNoCweId() {
            if (finding.getCweId() != null) {
                dump();
                autoDumper.execute(() -> fail("CWE id found inside finding:" + finding.getCweId()));
            }
            return this;
        }

        public AssertCodeCall codeCall(int level) {
            int currentLevel = 0;
            SecHubCodeCallStack code = finding.getCode();
            while (code != null && currentLevel != level) {
                code = code.getCalls();
                currentLevel++;
            }
            return new AssertCodeCall(code, currentLevel);
        }

        public class AssertCodeCall {

            private SecHubCodeCallStack callStack;

            public AssertCodeCall(SecHubCodeCallStack callStack, int level) {
                if (callStack == null) {
                    fail("Finding " + finding.getId() + " has no code call stack with level:" + level);
                }
                this.callStack = callStack;
            }

            public AssertFinding finding(int number) {
                return AssertReport.this.finding(number);
            }

            public AssertCodeCall hasLocation(String expected) {
                autoDumper.execute(() -> assertEquals(expected, callStack.getLocation()));
                return this;
            }

            public AssertCodeCall hasSource(String expected) {
                autoDumper.execute(() -> assertEquals(expected, callStack.getSource()));
                return this;
            }

            public AssertCodeCall hasRelevantPart(String expected) {
                autoDumper.execute(() -> assertEquals(expected, callStack.getRelevantPart()));
                return this;
            }

            public AssertCodeCall hasColumn(int column) {
                autoDumper.execute(() -> assertEquals(Integer.valueOf(column), callStack.getColumn()));
                return this;
            }

            public AssertCodeCall hasLine(int column) {
                autoDumper.execute(() -> assertEquals(Integer.valueOf(column), callStack.getLine()));
                return this;
            }

            public AssertFinding andFinding(int number) {
                return AssertReport.this.finding(number);
            }

            public AssertCodeCall codeCall(int level) {
                return AssertFinding.this.codeCall(level);
            }

            public AssertFinding andFinding() {
                return AssertFinding.this;
            }
        }

    }

    private List<SecHubFinding> assertFindings(SecHubReportData report) {
        assertNotNull("Report may not be null", report);
        SecHubResult result = report.getResult();
        autoDumper.execute(() -> assertNotNull(result));
        List<SecHubFinding> findings = result.getFindings();
        autoDumper.execute(() -> assertNotNull(findings));
        return findings;
    }

    public AssertReport hasJobUUID(UUID uuid) {
        assertEquals(uuid, report.getJobUUID());
        return this;
    }

    public AssertReport hasJobUUID(String uuidAsString) {
        return hasJobUUID(UUID.fromString(uuidAsString));
    }

    public AssertReport hasMetaDataLabel(String key, String value) {
        Optional<SecHubReportMetaData> metaDataOpt = report.getMetaData();
        if (metaDataOpt.isEmpty()) {
            fail("Meta data not found inside report!");
        }
        SecHubReportMetaData metaData = metaDataOpt.get();
        Map<String, String> labels = metaData.getLabels();
        if (!labels.containsKey(key)) {
            fail("Meta data labels do not contain key:" + key + "\nKeys found:" + labels.keySet());
        }
        String foundValue = labels.get(key);

        assertEquals(value, foundValue);

        return this;
    }

    public AssertReport dump() {
        LOG.info("-----------------------------------------------------------");
        LOG.info("----------------------------DUMP---------------------------");
        LOG.info("-----------------------------------------------------------");
        LOG.info("\n" + report.toFormattedJSON());
        LOG.info("-----------------------------------------------------------");
        return this;
    }

}
