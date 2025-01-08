// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoClassification;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoDetection;
import com.mercedesbenz.sechub.sereco.metadata.SerecoRevisionData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebBody;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebRequest;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebResponse;
import com.mercedesbenz.sechub.sereco.metadata.TestMetaDataAccess;

public class AssertVulnerabilities {

    private static final Logger LOG = LoggerFactory.getLogger(AssertVulnerabilities.class);

    private static VulnerabilityTestDescriptionBuilder descriptionBuilder = new VulnerabilityTestDescriptionBuilder();

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

    public static void assertWebRequest(SerecoVulnerability toInspect, SerecoWebRequest expectedRequest) {
        SerecoWeb vulnerabilityWeb = toInspect.getWeb();
        if (vulnerabilityWeb == null) {
            fail("vulnerability web is null!");
        }

        SerecoWebRequest foundRequest = vulnerabilityWeb.getRequest();
        if (!expectedRequest.equals(foundRequest)) {
            SerecoWebBody body1 = expectedRequest.getBody();
            SerecoWebBody body2 = foundRequest.getBody();
            internalAssertEquals(expectedRequest.getHeaders(), foundRequest.getHeaders(), "headers not as expected");
            internalAssertEquals(body1, body2, "body not as expected");

            fail("not equal but not detectable");
        }
    }

    public static void assertWebResponse(SerecoVulnerability toInspect, SerecoWebResponse expectedResponse) {
        SerecoWeb vulnerabilityWeb = toInspect.getWeb();
        if (vulnerabilityWeb == null) {
            fail("vulnerability web is null!");
        }
        SerecoWebResponse foundResponse = vulnerabilityWeb.getResponse();
        if (!expectedResponse.equals(foundResponse)) {
            SerecoWebBody body1 = expectedResponse.getBody();
            SerecoWebBody body2 = foundResponse.getBody();
            internalAssertEquals(expectedResponse.getHeaders(), foundResponse.getHeaders(), "headers not as expected");
            internalAssertEquals(body1, body2, "body not as expected");

            assertEquals("protocol", expectedResponse.getProtocol(), foundResponse.getProtocol());
            assertEquals("version", expectedResponse.getVersion(), foundResponse.getVersion());
            assertEquals("reasonPhrase", expectedResponse.getReasonPhrase(), foundResponse.getReasonPhrase());
            assertEquals("statusCode", expectedResponse.getStatusCode(), foundResponse.getStatusCode());

            fail("not equal but not detectable");
        }

    }

    private static void internalAssertEquals(Object obj1, Object obj2, String message) {
        if (Objects.equals(obj1, obj2)) {
            return;
        }
        if (obj1 instanceof Map) {
            obj1 = asSortedMap((Map<?, ?>) obj1);
        }
        if (obj2 instanceof Map) {
            obj2 = asSortedMap((Map<?, ?>) obj2);
        }
        assertEquals(message, Objects.toString(obj1), Objects.toString(obj2));
    }

    private static SortedMap<?, ?> asSortedMap(Map<?, ?> map) {
        if (map instanceof SortedMap) {
            return (SortedMap<?, ?>) map;
        }
        return new TreeMap<>(map);

    }

    public class VulnerabilityFinder {

        private SerecoVulnerability search;
        private boolean traceEnabled;

        private VulnerabilityFinder() {
            /*
             * we use null values for search to search only for wanted parts...,+ some
             * defaults (empty list) and empty description
             */
            String type = null;
            SerecoSeverity severity = null;
            List<SerecoDetection> list = new ArrayList<>();
            String description = "";
            SerecoClassification classification = null;

            search = TestMetaDataAccess.createVulnerability(type, severity, list, description, classification);
        }

        private List<SerecoVulnerability> find(StringBuilder message) {
            List<SerecoVulnerability> list = find(message, false);
            if (list.isEmpty()) {
                find(message, true);
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
                contained = contained && isEitherNullInSearchOrEqual(search.getType(), vulnerability.getType()) && trace.done(vulnerability, "type");
                contained = contained && isEitherNullInSearchOrContains(vulnerability.getDescription(), search.getDescription()) && trace.done(vulnerability, "description");
                contained = contained && isEitherNullInSearchOrEqual(search.getClassification(), vulnerability.getClassification()) && trace.done(vulnerability, "classification");
                contained = contained && isEitherNullInSearchOrEqual(search.getCode(), vulnerability.getCode()) && trace.done(vulnerability, "code");
                contained = contained && isEitherNullInSearchOrEqual(search.getScanType(), vulnerability.getScanType()) && trace.done(vulnerability, "scanType");
                contained = contained && isEitherNullInSearchOrEqual(search.getWeb(), vulnerability.getWeb()) && trace.done(vulnerability, "web");
                contained = contained && isEitherNullInSearchOrEqual(search.getRevision(), vulnerability.getRevision()) && trace.done(vulnerability, "revision");
                /* @formatter:on */
                if (contained) {
                    matching.add(vulnerability);
                }
            }
            if (findClosest) {
                SerecoVulnerability closest = trace.getClosest();
                message.append(">> Closest vulnerability was:\n" + describe(closest) + "\n\n>>>There was last ok:" + trace.getClosestLastCheck());

            }
            return matching;
        }

        public String describe(SerecoVulnerability vulnerability) {
            return descriptionBuilder.describe(vulnerability);
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

        public VulnerabilityFinder withType(String type) {
            search.setType(type);
            return this;
        }

        public VulnerabilityFinder withScanType(ScanType scanType) {
            search.setScanType(scanType);
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

            public FindCodeCallStackBuilder containingSource(String source) {
                currentCallStackElement.setSource(source);
                return this;
            }

            public FindCodeCallStackBuilder withRevisionId(String revisionId) {
                SerecoRevisionData revision = new SerecoRevisionData();
                revision.setId(revisionId);
                search.setRevision(revision);
                return this;
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
            assertEquals("Not found expected amount of vulnerabilities for given search.\n>>Searched for:\n" + describe(search) + " \n" + message.toString()
                    + "\n (vulnerabilitis found at all:" + matches.size() + ", false positives:" + falsePositives + ")", expectedAmount, check);
            throw new IllegalStateException("Test must fail before by assertEquals!");
        }

        public class AssertClassification {

            private SerecoClassification classification;

            private AssertClassification() {
                classification = new SerecoClassification();
                TestMetaDataAccess.setClassification(VulnerabilityFinder.this.search, classification);
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

        /**
         * When this method is used the web vulnerability data must be defined 100%
         * correctly by dedicated with... methods. Otherwise this method will always
         * fail!
         *
         * @return
         */
        public WebVulnerabilityFinder isExactDefinedWebVulnerability() {
            SerecoWeb web = new SerecoWeb();
            search.setWeb(web);
            return new WebVulnerabilityFinder();
        }

        public class WebVulnerabilityFinder {

            public WebVulnerabilityFinder withTarget(String target) {
                search.getWeb().getRequest().setTarget(target);
                return this;
            }

            public WebVulnerabilityFinder withWebRequest(SerecoWebRequest webRequest) {
                TestMetaDataAccess.setWebRequest(search, webRequest);
                return this;
            }

            public WebVulnerabilityFinder withWebResponse(SerecoWebResponse webResponse) {
                TestMetaDataAccess.setWebResponse(search, webResponse);
                return this;
            }

            public VulnerabilityFinder and() {
                return VulnerabilityFinder.this;
            }

            public AssertVulnerabilities isContained() {
                return VulnerabilityFinder.this.isContained();
            }

            public SerecoVulnerability assertContainedAndReturn() {
                return VulnerabilityFinder.this.assertContainedAndReturn();
            }

        }

        public SerecoVulnerability assertContainedAndReturn() {
            StringBuilder message = new StringBuilder();
            List<SerecoVulnerability> matches = find(message);
            if (matches.size() != 1) {
                assertEquals(message.toString(), 1, matches.size());
            }
            return matches.get(0);
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

            // Check if CWE is already in the map
            // NOTE: Using computeIfAbsent can cause a ConcurrentModificationException,
            // except if the much slower ConcurrentSkipListMap is used
            if (!map.containsKey(cweNumber)) {
                // add CWE to the map
                map.put(cweNumber, new LinkedList<SerecoVulnerability>());
            }

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

    public AssertVulnerabilities dump() {
        dump(vulnerabilities);
        return this;
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