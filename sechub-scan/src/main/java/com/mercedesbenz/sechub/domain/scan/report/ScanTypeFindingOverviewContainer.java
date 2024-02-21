// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.Map;
import java.util.TreeMap;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.ScanTypeSummaryFindingOverviewData;
import com.mercedesbenz.sechub.commons.model.Severity;

/**
 * Class to hold finding overviews for different severities for one scan type
 *
 */
public class ScanTypeFindingOverviewContainer {

    private Map<Severity, Map<String, ScanTypeSummaryFindingOverviewData>> severityToMapMap = new TreeMap<>();

    private ScanType scanType;

    public ScanTypeFindingOverviewContainer(ScanType scanType) {
        this.scanType = scanType;

        /* initialize mapping for all severities */
        for (Severity severity : Severity.values()) {
            Map<String, ScanTypeSummaryFindingOverviewData> nameToOverviewDataMap = new TreeMap<>();
            severityToMapMap.put(severity, nameToOverviewDataMap);
        }

    }

    public ScanType getScanType() {
        return scanType;
    }

    public Map<String, ScanTypeSummaryFindingOverviewData> getMapForSeverity(Severity severity) {
        return severityToMapMap.get(severity);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": scanType=" + getScanType();
    }
}