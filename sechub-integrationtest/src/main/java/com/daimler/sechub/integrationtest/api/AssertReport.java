// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubReport;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.commons.model.TrafficLight;

public class AssertReport {

    private static final Logger LOG = LoggerFactory.getLogger(AssertReport.class);

    private SecHubReport report;

    public static AssertReport assertReport(String json) {
        return new AssertReport(SecHubReport.fromJSONString(json));
    }
    
    AssertReport(SecHubReport report) {
        assertNotNull("Report may not be null", report);
        this.report = report;
    }

    public AssertReport hasFindings(int expectedCount) {
        List<SecHubFinding> findings = assertFindings(report);
        assertEquals(expectedCount, findings.size());
        return this;
    }

    public AssertReport hasTrafficLight(TrafficLight expectedLight) {
        assertEquals(expectedLight, report.getTrafficLight());
        return this;
    }

    public AssertFinding finding(int number) {
        SecHubFinding secHubFinding = assertFindings(report).get(number);
        return new AssertFinding(secHubFinding,number);
    }
    

    public class AssertFinding {

        private SecHubFinding finding;

        public AssertFinding(SecHubFinding finding, int number) {
            assertNotNull("Finding may not be null! But was for number:"+number, finding);
            this.finding = finding;
        }

        public AssertReport andReport() {
            return AssertReport.this;
        }
        
        public AssertFinding hasId(int id) {
            assertEquals(id, finding.getId());
            return this;
        }

        public AssertFinding hasName(String name) {
            assertEquals(name, finding.getName());
            return this;
        }
        
        public AssertFinding hasScanType(ScanType type) {
            assertEquals(type, finding.getType());
            return this;
        }

        public AssertFinding hasDescription(String description) {
            assertEquals(description, finding.getDescription());
            return this;
        }

        
        public AssertFinding hasNoHostnames() {
            return hasHostnames();
        }
        public AssertFinding hasHostnames(String... hostnames) {
            List<String> hostnames2 = finding.getHostnames();

            for (String hostname : hostnames) {
                if (!hostnames2.contains(hostname)) {
                    fail("Hostname:" + hostname + " not found in finding!");
                }
            }
            assertEquals(hostnames.length, hostnames2.size());
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
                    fail("Reference:" + reference + " not found in finding!");
                }
            }
            assertEquals(references.length, references2.size());
            return this;
        }

        public AssertFinding hasSeverity(Severity severity) {
            if (!Objects.equals(severity, finding.getSeverity())){
                dump();
                assertEquals("Finding id:"+finding.getId()+" has not expected severity!", severity,finding.getSeverity());
            }
            return this;
        }
        
        public AssertCodeCall codeCall(int level) {
            int currentLevel=0;
            SecHubCodeCallStack code = finding.getCode();
            while (code!=null && currentLevel!=level) {
                code = code.getCalls();
                currentLevel++;
            }
            return new AssertCodeCall(code,currentLevel);
        }
        
        public class AssertCodeCall{
            
            private SecHubCodeCallStack callStack;

            public AssertCodeCall(SecHubCodeCallStack callStack, int level) {
                if (callStack==null) {
                    fail("Finding "+finding.getId()+" has no code call stack with level:"+level);
                }
                this.callStack=callStack;
            }
            
            public AssertFinding finding(int number) {
                return AssertReport.this.finding(number);
            }

            public AssertCodeCall hasLocation(String expected) {
                assertEquals(expected,callStack.getLocation());
                return this;
            }
            
            public AssertCodeCall hasSource(String expected) {
                assertEquals(expected,callStack.getSource());
                return this;
            }
            
            public AssertCodeCall hasRelevantPart(String expected) {
                assertEquals(expected,callStack.getRelevantPart());
                return this;
            }
            
            public AssertCodeCall hasColumn(int column) {
                assertEquals(Integer.valueOf(column),callStack.getColumn());
                return this;
            }
            
            public AssertCodeCall hasLine(int column) {
                assertEquals(Integer.valueOf(column),callStack.getLine());
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

    private List<SecHubFinding> assertFindings(SecHubReport report) {
        assertNotNull("Report may not be null", report);
        SecHubResult result = report.getResult();
        assertNotNull(result);
        List<SecHubFinding> findings = result.getFindings();
        assertNotNull(findings);
        return findings;
    }

    public AssertReport hasJobUUID(String uuidAsString) {
        assertEquals(UUID.fromString(uuidAsString), report.getJobUUID());
        return this;
    }

    public AssertReport dump() {
        LOG.info("-----------------------------------------------------------");
        LOG.info("----------------------------DUMP---------------------------");
        LOG.info("-----------------------------------------------------------");
        LOG.info(report.toJSON());
        LOG.info("-----------------------------------------------------------");
        return this;
    }
}
