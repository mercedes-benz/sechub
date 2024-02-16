package com.mercedesbenz.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;

/**
 * Represents an entry inside a HTMLTrafficlightFindingGroup
 *
 * <pre>
 *
 *  CodeSCan                    <--- HTMLTrafficlightScanTypeFindingData
 *     | ID | Severity | Description
 *     |----------------------------------------
 *     | 1     Critical    xxx      <---- SecHubFinding
 *     | 11     High    xxx      <---- SecHubFinding
 *
 * </pre>
 *
 */
public class HTMLTrafficlightScanTypeFindingData implements Comparable<HTMLTrafficlightScanTypeFindingData> {
    private static SecHubFindingByIdComparator SEVERITY_THEN_FINDING_ID_COMPARATOR = new SecHubFindingByIdComparator();
    private ScanType scanType;
    private List<SecHubFinding> relatedFindings = new ArrayList<>();
    private Map<Severity, SecHubFinding> firstOfSeverityMap = new LinkedHashMap<>(Severity.values().length);

    HTMLTrafficlightScanTypeFindingData(ScanType scanType) {
        Objects.requireNonNull(scanType, "ScanType may not be null!");
        this.scanType = scanType;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public List<SecHubFinding> getRelatedFindings() {
        List<SecHubFinding> sortedFindingsById = new ArrayList<>(relatedFindings);
        Collections.sort(sortedFindingsById, SEVERITY_THEN_FINDING_ID_COMPARATOR);
        return sortedFindingsById;
    }

    @Override
    public int compareTo(HTMLTrafficlightScanTypeFindingData o) {
        return scanType.compareTo(o.scanType);
    }

    @Override
    public String toString() {
        return "HTMLTrafficlightScanTypeFindingData [" + (scanType != null ? "scanType=" + scanType + ", " : "")
                + (relatedFindings != null ? "relatedFindings=" + relatedFindings : "") + "]";
    }

    void addRelatedFinding(SecHubFinding finding) {
        if (finding == null) {
            return;
        }
        relatedFindings.add(finding);

        Severity severity = finding.getSeverity();
        if (severity == null) {
            return;
        }
        SecHubFinding firstEntry = firstOfSeverityMap.computeIfAbsent(severity, s -> finding);
        if (firstEntry.getId() > finding.getId()) {
            firstOfSeverityMap.put(severity, finding);
        }
    }

    public boolean isNotFirstLinkItem(SecHubFinding finding) {
        return !isFirstLinkItem(finding);
    }

    public boolean isFirstLinkItem(SecHubFinding finding) {
        return firstOfSeverityMap.containsValue(finding);
    }

    private static class SecHubFindingByIdComparator implements Comparator<SecHubFinding> {

        @Override
        public int compare(SecHubFinding o1, SecHubFinding o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            /* compare severity reverse (CRITICAL on top) */
            int result = o2.getSeverity().compareTo(o1.getSeverity());
            if (result != 0) {
                return result;
            }
            /* ids top down */
            result = o1.getId() - o2.getId();
            return result;
        }

    }

}