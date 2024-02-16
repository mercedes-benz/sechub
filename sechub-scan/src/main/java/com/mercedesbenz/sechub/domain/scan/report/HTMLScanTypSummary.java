package com.mercedesbenz.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;

/**
 * Class and its subclasses to hold HTML finding overview data for different
 * severities for one scan type
 *
 * For easier maintenance, this is a completely separate data model that is only
 * used for is used to render HTML reports and has very few dependencies on the
 * JSON model.
 *
 * Example:
 *
 * <pre>
 * Summary
 *                        Critical High Medium Low Unclassified Info
 * -------------------------------------------------------------------------------------------------
 * ScanType: Web scan   |     0     1     0     1      0        0  <- HTMLScanTypSummary
 * -------------------------------------------------------------------------------------------------
 * v WebScan overview   <------------ HTMLScanTypSummary instance (here scanType: WebScan)
 *   ----------------
 *   High                             <---- HTMLScanTypeSeveritySummary
 *     CWE          Name                        count
 *     --------------------------------------------------------
 *      CWE-89      v SQL-Injection SQLite        2    <--- HTMLFindingSummary
 *                    -----------------------------------------
 *                     Id                 Location
 *                     1                  https://localhost:3000  <--- additional
 *                     2                  https://localhost:3000
 *   Medium
 *      ...
 *
 *   Low
 *      ...
 *   Unclassified
 *      ...
 * </pre>
 */
public class HTMLScanTypSummary {

    private Map<Severity, HTMLScanTypeSeveritySummary> severityOverviewMap = new TreeMap<>();

    private ScanType scanType;

    public HTMLScanTypSummary(ScanType scanType) {
        this.scanType = scanType;
    }

    public String getScanTypeName() {
        return scanType.getText();
    }

    public ScanType getScanType() {
        return scanType;
    }

    public HTMLScanTypeSeveritySummary ensureSeveritySummary(Severity severity) {
        return severityOverviewMap.computeIfAbsent(severity, s -> new HTMLScanTypeSeveritySummary(s));
    }

    /**
     * Resolves all severity summaries for this scan type in a sorted way
     *
     * @return unmodifiable sorted collection with
     *         {@link HTMLScanTypeSeveritySummary} elements
     */
    public SortedSet<HTMLScanTypeSeveritySummary> getSeveritySummaries() {
        return new TreeSet<>(severityOverviewMap.values());
    }

    public String getHeadline() {
        return getScanTypeName() + " overview";
    }

    public long getTotalCount() {
        return count(Severity.values());
    }

    public long getCriticalSeverityCount() {
        return count(Severity.CRITICAL);
    }

    public long getHighSeverityCount() {
        return count(Severity.HIGH);
    }

    public long getMediumSeverityCount() {
        return count(Severity.MEDIUM);
    }

    public long getLowSeverityCount() {
        return count(Severity.LOW);
    }

    public long getUnclassifiedSeverityCount() {
        return count(Severity.UNCLASSIFIED);
    }

    public long getInfoSeverityCount() {
        return count(Severity.INFO);
    }

    public String getLinkToFirstCritical() {
        return getLinkSupport().createLinkToFirstOf(scanType, Severity.CRITICAL);
    }

    public String getLinkToFirstHigh() {
        return getLinkSupport().createLinkToFirstOf(scanType, Severity.HIGH);
    }

    public String getLinkToFirstMedium() {
        return getLinkSupport().createLinkToFirstOf(scanType, Severity.MEDIUM);
    }

    public String getLinkToFirstLow() {
        return getLinkSupport().createLinkToFirstOf(scanType, Severity.LOW);
    }

    public String getLinkToFirstUnclassified() {
        return getLinkSupport().createLinkToFirstOf(scanType, Severity.UNCLASSIFIED);
    }

    public String getLinkToFirstInfo() {
        return getLinkSupport().createLinkToFirstOf(scanType, Severity.INFO);
    }

    private HTMLFirstLinkToSeveritySupport getLinkSupport() {
        return HTMLFirstLinkToSeveritySupport.DEFAULT;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": scanType=" + getScanType();
    }

    private long count(Severity... severities) {
        long count = 0;
        for (Severity severity : severities) {
            HTMLScanTypeSeveritySummary severitySummary = severityOverviewMap.get(severity);
            if (severitySummary != null) {
                count += severitySummary.calculateSeverityCount();
            }
        }
        return count;

    }

    public class HTMLScanTypeSeveritySummary implements Comparable<HTMLScanTypeSeveritySummary> {
        private Severity severity;

        public HTMLScanTypeSeveritySummary(Severity severity) {
            this.severity = severity;
        }

        private Map<String, HTMLFindingSummary> findingNameToFindingSummary = new TreeMap<>();

        public Severity getSeverity() {
            return severity;
        }

        /**
         * @return unmodifiable collection of finding summaries
         */
        public Collection<HTMLFindingSummary> getFindingSummaries() {
            return Collections.unmodifiableCollection(findingNameToFindingSummary.values());
        }

        public long calculateSeverityCount() {
            long allEntriesCount = 0;
            Iterator<HTMLFindingSummary> it = findingNameToFindingSummary.values().iterator();
            while (it.hasNext()) {
                HTMLFindingSummary entry = it.next();
                allEntriesCount += entry.getCount();
            }

            return allEntriesCount;
        }

        /**
         * Adds finding to corresponding finding summary - if no summary exists it will
         * be created.
         *
         * @param finding finding to add
         */
        void addFinding(SecHubFinding finding) {
            String name = finding.getName();
            if (name == null) {
                name = "";
            }
            Integer cweId = finding.getCweId();

            HTMLFindingSummary findingSummary = findingNameToFindingSummary.computeIfAbsent(name, n -> new HTMLFindingSummary(n, cweId));

            findingSummary.getRelatedFindings().add(finding);
        }

        @Override
        public int compareTo(HTMLScanTypeSeveritySummary other) {
            if (other == null) {
                return 1;
            }
            if (other.severity == severity) {
                return 0;
            }
            if (other.severity == null) {
                return 1;
            }
            if (severity == null) {
                return -1;
            }
            /* CRITICAL --> HIGH --> ... ---> INFO */
            return other.severity.getLevel() - severity.getLevel();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Objects.hash(severity);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            HTMLScanTypeSeveritySummary other = (HTMLScanTypeSeveritySummary) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
                return false;
            }
            return severity == other.severity;
        }

        private HTMLScanTypSummary getEnclosingInstance() {
            return HTMLScanTypSummary.this;
        }

    }

    public class HTMLFindingSummary {

        private String name;

        private Integer cweId;

        public HTMLFindingSummary(String name, Integer cweId) {
            this.name = name;
            this.cweId = cweId;
        }

        public void setCweId(Integer cweId) {
            this.cweId = cweId;
        }

        public Integer getCweId() {
            return cweId;
        }

        public String getName() {
            return name;
        }

        public String getCweText() {
            if (cweId == null) {
                return "";
            }
            return "CWE-" + cweId;
        }

        public long getCount() {
            return relatedFindings.size();
        }

        // we use a tree set to get the findings sorted automatically
        private List<SecHubFinding> relatedFindings = new ArrayList<>();

        public List<SecHubFinding> getRelatedFindings() {
            return relatedFindings;
        }
    }

    public void add(SecHubFinding finding) {
        if (finding == null) {
            return;
        }
        Severity severity = finding.getSeverity();
        if (severity == null) {
            severity = Severity.UNCLASSIFIED;
        }
        HTMLScanTypeSeveritySummary scanTypeSeveritySummary = ensureSeveritySummary(severity);
        scanTypeSeveritySummary.addFinding(finding);

    }
}