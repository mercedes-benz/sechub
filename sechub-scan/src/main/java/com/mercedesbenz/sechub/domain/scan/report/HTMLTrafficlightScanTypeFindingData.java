package com.mercedesbenz.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;

public class HTMLTrafficlightScanTypeFindingData implements Comparable<HTMLTrafficlightScanTypeFindingData> {
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
        return Collections.unmodifiableList(relatedFindings);
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
        if (finding==null) {
            return;
        }
        relatedFindings.add(finding);
        
        Severity severity = finding.getSeverity();
        if (severity==null) {
            return;
        }
        /* next line does only create missing key,value entry, when key does not exist,
         * means: only the first entry is present inside this map!
         */
        firstOfSeverityMap.computeIfAbsent(severity, s -> finding);
    }
    
    public boolean isNotFirstLinkItem(SecHubFinding finding) {
        return ! isFirstLinkItem(finding);
    }
    
    public boolean isFirstLinkItem(SecHubFinding finding) {
        return firstOfSeverityMap.containsValue(finding);
    }

}