// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.Severities;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

@Component
public class ScanReportTrafficLightCalculator {

    public TrafficLight calculateTrafficLight(SecHubReportModel result) {
        if (result == null) {
            throw new IllegalArgumentException("SecHub result may not be null!");
        }
        TrafficLight trafficLight = null;
        for (Severity severity : Severities.getAllOrderedFromHighToLow()) {
            trafficLight = resolveTrafficLightWhenOneEntryWithSuchSeverity(result, trafficLight, severity);
        }
        if (trafficLight != null) {
            return trafficLight;
        }
        /* when no severities... */
        return TrafficLight.GREEN;
    }

    TrafficLight resolveTrafficLightWhenOneEntryWithSuchSeverity(SecHubReportModel result, TrafficLight found, Severity severity) {
        if (found != null) {
            return found;
        }
        if (!hasResultOneFindingWith(result, severity)) {
            return null;
        }
        return mapToTrafficLight(severity);
    }

    TrafficLight mapToTrafficLight(Severity severity) {
        if (Severity.CRITICAL.equals(severity)) {
            return TrafficLight.RED;
        }
        if (Severity.HIGH.equals(severity)) {
            return TrafficLight.RED;
        }
        if (Severity.MEDIUM.equals(severity)) {
            return TrafficLight.YELLOW;
        }
        return TrafficLight.GREEN;
    }

    public List<SecHubFinding> filterFindingsFor(SecHubResult result, TrafficLight searched) {
        List<SecHubFinding> filteredResult = new ArrayList<>();
        if (searched == null) {
            return filteredResult;
        }
        for (SecHubFinding finding : result.getFindings()) {
            TrafficLight trafficLightFound = mapToTrafficLight(finding.getSeverity());
            if (searched.equals(trafficLightFound)) {
                filteredResult.add(finding);
            }
        }
        return filteredResult;
    }

    private boolean hasResultOneFindingWith(SecHubReportModel result, Severity severity) {
        if (severity == null) {
            return false;
        }
        List<SecHubFinding> findings = result.getResult().getFindings();
        if (findings == null || findings.isEmpty()) {
            return false;
        }
        for (SecHubFinding finding : findings) {
            if (finding == null) {
                continue;
            }
            if (severity.equals(finding.getSeverity())) {
                return true;
            }
        }
        return false;
    }

}
