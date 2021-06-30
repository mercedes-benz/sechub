// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.core.util.SimpleStringUtils;
import com.daimler.sechub.sereco.metadata.MetaDataAccess;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoDetection;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class AssertVulnerabilities {

    private static final Logger LOG = LoggerFactory.getLogger(AssertVulnerabilities.class);

    private List<SerecoVulnerability> vulnerabilities = new ArrayList<>();

    AssertVulnerabilities(List<SerecoVulnerability> list) {
        this.vulnerabilities.addAll(list);
    }

    public static AssertVulnerabilities assertVulnerabilities(List<SerecoVulnerability> list) {
        return new AssertVulnerabilities(list);
    }

    /**
     * Same as {@link #vulnerability()} but easier to understand in tests and more
     * like a "good taled story" part.
     * 
     * @return
     */
    public VulnerabilityFinder verifyVulnerability() {
        return vulnerability();
    }

    public VulnerabilityFinder vulnerability() {
        return new VulnerabilityFinder();
    }

    public class VulnerabilityFinder {

        private SerecoVulnerability search;
        private boolean traceEnabled;

        private VulnerabilityFinder() {
            /*
             * we use null values for search to search only for wanted parts...,+ some
             * defaults (empty list) and empty description
             */
            String url = null;
            String type = null;
            SerecoSeverity severity = null;
            List<SerecoDetection> list = new ArrayList<>();
            String description = "";
            SerecoClassification classification = null;

            search = MetaDataAccess.createVulnerability(url, type, severity, list, description, classification);
            ;
        }

        private List<SerecoVulnerability> find(StringBuilder metaInfo) {
            List<SerecoVulnerability> list = find(metaInfo, false);
            if (list.isEmpty()) {
                find(metaInfo, true);
            }
            return list;
        }

        private boolean isEitherNullInSearchOrEqual(Object search, Object data) {
            if (search == null) {
                return true;
            }
            return TestUtils.equals(search, data);
        }

        private boolean isEitherNullInSearchOrContains(String string, String partWhichShallBeContained) {
            if (search == null) {
                return true;
            }
            return TestUtils.contains(string, partWhichShallBeContained);
        }

        private List<SerecoVulnerability> find(StringBuilder message, boolean findClosest) {
            List<SerecoVulnerability> matching = new ArrayList<>();
            SearchTracer trace = new SearchTracer();
            trace.traceEnabled = traceEnabled;
            for (SerecoVulnerability vulnerability : AssertVulnerabilities.this.vulnerabilities) {
                boolean contained = isEitherNullInSearchOrEqual(search.getSeverity(), vulnerability.getSeverity()) && trace.done(vulnerability, "severity");
                /* @formatter:off */
                contained = contained && isEitherNullInSearchOrEqual(search.getUrl(), vulnerability.getUrl()) && trace.done(vulnerability, "url");
                contained = contained && isEitherNullInSearchOrEqual(search.getType(), vulnerability.getType()) && trace.done(vulnerability, "type");
                contained = contained && isEitherNullInSearchOrContains(vulnerability.getDescription(), search.getDescription()) && trace.done(vulnerability, "description");
                contained = contained && isEitherNullInSearchOrEqual(search.getClassification(), vulnerability.getClassification()) && trace.done(vulnerability, "classification");
                contained = contained && isEitherNullInSearchOrEqual(search.getCode(), vulnerability.getCode()) && trace.done(vulnerability, "code");
                /* @formatter:on */
                if (contained) {
                    matching.add(vulnerability);
                }
            }
            if (findClosest) {
                message.append("Closest vulnerability was:\n" + trace.getClosest() + "\nThere was last ok:" + trace.getClosestLastCheck());
            }
            return matching;
        }

        private class SearchTracer {
            private boolean traceEnabled;
            private int closestNumber = 0;
            private int vulnerabilityNumber = 0;
            private int count = 0;
            private SerecoVulnerability closest;
            private SerecoVulnerability last;
            private int closestCount = 0;
            private String closestLastCheck;

            boolean done(SerecoVulnerability v, String description) {
                if (traceEnabled && closest != null) {
                    LOG.debug("Current closest ({}) type:{},severity:{},count={}", closestNumber, closest.getType(), closest.getSeverity(), closestCount);
                }
                if (last != v) {
                    if (traceEnabled) {
                        LOG.debug("Inspect next vulnerability({})", vulnerabilityNumber);
                    }

                    count = 0;
                    vulnerabilityNumber++;
                }
                last = v;
                count++;

                if (traceEnabled) {
                    LOG.debug("Inspect vulnerability({})type:{},severity:{},count={},description={}", vulnerabilityNumber, v.getType(), v.getSeverity(), count,
                            description);
                }
                if (count > closestCount) {
                    closestCount = count;
                    closestNumber = vulnerabilityNumber;
                    closest = v;
                    closestLastCheck = description;
                    if (traceEnabled) {
                        LOG.debug("New closest count set({}) for type:{},severity:{},count={},description={}", vulnerabilityNumber, closest.getType(),
                                closest.getSeverity(), closestCount, description);
                    }
                }

                return true;
            }

            public SerecoVulnerability getClosest() {
                return closest;
            }

            public String getClosestLastCheck() {
                return closestLastCheck;
            }

        }

        public VulnerabilityFinder withSeverity(SerecoSeverity severity) {
            search.setSeverity(severity);
            return this;
        }

        /**
         * Enable trace logging - shows inspection information in case of error handling
         * / understanding of verification
         * 
         * @return assert object itself
         */
        public VulnerabilityFinder enableTrace() {
            traceEnabled = true;
            return this;
        }

        public VulnerabilityFinder withURL(String url) {
            search.setUrl(url);
            return this;
        }

        public VulnerabilityFinder withType(String type) {
            search.setType(type);
            return this;
        }

        public FindCodeCallStackBuilder withCodeLocation(String location, int line, int column) {
            return new FindCodeCallStackBuilder(location, line, column);
        }

        public class FindCodeCallStackBuilder {

            private SerecoCodeCallStackElement currentCallStackElement;

            public FindCodeCallStackBuilder(String location, int line, int column) {
                currentCallStackElement = createCallStackElement(location, line, column);

                search.setCode(currentCallStackElement);
            }

            public FindCodeCallStackBuilder calling(String location, int line, int column) {
                SerecoCodeCallStackElement childCallstackElement = createCallStackElement(location, line, column);

                currentCallStackElement.setCalls(childCallstackElement);
                currentCallStackElement = childCallstackElement;
                return this;
            }

            public VulnerabilityFinder done() {
                return VulnerabilityFinder.this;
            }

            private SerecoCodeCallStackElement createCallStackElement(String location, int line, int column) {
                SerecoCodeCallStackElement newCallStackElement = new SerecoCodeCallStackElement();

                newCallStackElement.setLocation(location);
                newCallStackElement.setLine(line);
                newCallStackElement.setColumn(column);

                return newCallStackElement;
            }

        }

        public VulnerabilityFinder withDescriptionContaining(String descriptionPart) {
            search.setDescription(descriptionPart);
            return this;
        }

        public AssertClassification classifiedBy() {
            return new AssertClassification();
        }

        public AssertVulnerabilities isNotContained() {
            return isNotContained(false);
        }

        public AssertVulnerabilities isNotContained(boolean ignoreWhenFalsePositive) {
            return isContained(0, ignoreWhenFalsePositive);
        }

        /**
         * Assert vulnerability is contained - false
         * 
         * @return
         */
        public AssertVulnerabilities isContained() {
            return isContained(false);
        }

        public AssertVulnerabilities isContained(boolean ignoreWhenFalsePositive) {
            return isContained(1, ignoreWhenFalsePositive);
        }

        private AssertVulnerabilities isContained(int expectedAmount, boolean ignoreWhenFalsePositive) {
            StringBuilder message = new StringBuilder();
            List<SerecoVulnerability> matches = find(message);
            int falsePositives = countFalsePositives(matches);
            int check = matches.size();
            if (ignoreWhenFalsePositive) {
                check = check - falsePositives;
            }
            if (check == expectedAmount) {
                return AssertVulnerabilities.this;
            }
            dump(vulnerabilities);

            StringBuilder sb = new StringBuilder();
            for (SerecoVulnerability v : vulnerabilities) {
                sb.append(v.toString());
                sb.append("\n");
            }
            assertEquals("Not found expected amount of vulnerabilities for given search.\nSearched for:\n" + search + " \n" + message.toString()
                    + "\n (vulnerabilitis found at all:" + matches.size() + ", false positives:" + falsePositives + ")", expectedAmount, check);
            throw new IllegalStateException("Test must fail before by assertEquals!");
        }

        public class AssertClassification {

            private SerecoClassification classification;

            private AssertClassification() {
                classification = new SerecoClassification();
                MetaDataAccess.setClassification(VulnerabilityFinder.this.search, classification);
            }

            public AssertClassification hipaa(String hipaa) {
                classification.setHipaa(hipaa);
                return this;
            }

            public AssertClassification owaspProactiveControls(String owaspProactiveControls) {
                classification.setOwaspProactiveControls(owaspProactiveControls);
                return this;
            }

            public AssertClassification pci31(String pci31) {
                classification.setPci31(pci31);
                return this;
            }

            public AssertClassification pci32(String pci32) {
                classification.setPci32(pci32);
                return this;
            }

            public AssertClassification cwe(String cwe) {
                classification.setCwe(cwe);
                return this;
            }

            public AssertClassification cwe(int cwe) {
                return cwe("" + cwe);
            }

            public AssertClassification capec(String capec) {
                classification.setCapec(capec);
                return this;
            }

            public AssertClassification owasp(String owasp) {
                classification.setOwasp(owasp);
                return this;
            }

            public AssertClassification wasc(String wasc) {
                classification.setWasc(wasc);
                return this;
            }

            public VulnerabilityFinder and() {
                return VulnerabilityFinder.this;
            }

        }

    }

    private static void dump(List<SerecoVulnerability> vulnerabilities) {
        StringBuilder sb = new StringBuilder();

        SortedMap<Integer, List<SerecoVulnerability>> map = new TreeMap<>();
        for (SerecoVulnerability vulnerability : vulnerabilities) {
            String cwe = vulnerability.getClassification().getCwe();
            if (cwe == null) {
                cwe = "0";
            }
            Integer cweNumber = Integer.valueOf(cwe);
            map.computeIfAbsent(cweNumber, (key) -> map.put(key, new ArrayList<SerecoVulnerability>()));
            map.get(cweNumber).add(vulnerability);
        }

        map.values().forEach((list) -> {
            SerecoVulnerability firstVulnerabilityInList = list.iterator().next();
            sb.append("CWE " + firstVulnerabilityInList.getClassification().getCwe() + " \"" + firstVulnerabilityInList.getType() + "\" found " + list.size())
                    .append(" times:\n");
            list.forEach((vulnerability) -> {
                sb.append("- CWE=").append(vulnerability.getClassification().getCwe());
                sb.append(',').append(SimpleStringUtils.truncateWhenTooLong(vulnerability.getType(), 10));
                sb.append("\n");
                sb.append("    |->").append(vulnerability);
                sb.append("\n");
                SerecoCodeCallStackElement element = vulnerability.getCode();
                int step = 0;
                while (element != null) {
                    step++;
                    sb.append(step).append(':');
                    sb.append("  |-- location=").append(element.getLocation());
                    sb.append(", line=").append(element.getLine()).append(", column=").append(element.getColumn());
                    sb.append("\n");
                    element = element.getCalls();
                }

                sb.append("\n");
            });
        });

        LOG.info("-----------------------------------------------------------");
        LOG.info("----------------------------DUMP---------------------------");
        LOG.info("-----------------------------------------------------------");
        LOG.info(sb.toString());
        LOG.info("-----------------------------------------------------------");
    }

    public AssertVulnerabilities hasVulnerabilities(int expectedAmount) {
        return hasVulnerabilities(expectedAmount, false);

    }

    public AssertVulnerabilities hasVulnerabilities(int expectedAmount, boolean ignoreFalsePositives) {
        int check = vulnerabilities.size();
        if (ignoreFalsePositives) {
            check = check - countFalsePositives(vulnerabilities);
        }
        assertEquals("Amount of vulnerabilities differs", expectedAmount, check);
        return this;

    }

    private int countFalsePositives(List<SerecoVulnerability> matches) {
        AtomicInteger countOfFalsePositives = new AtomicInteger(0);
        matches.forEach(v -> {
            if (v.isFalsePositive()) {
                countOfFalsePositives.getAndIncrement();
            }
        });
        return countOfFalsePositives.get();
    }

}