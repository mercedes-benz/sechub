// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightSupport implements TrafficLightCalculator, SecHubResultTrafficLightFilter {

    public TrafficLight calculateTrafficLight(SecHubResult result) {
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

    TrafficLight resolveTrafficLightWhenOneEntryWithSuchSeverity(SecHubResult result, TrafficLight found, Severity severity) {
        if (found != null) {
            return found;
        }
        if (!hasResultOneFindingWith(result, severity)) {
            return null;
        }
        return mapToTrafficLight(severity);
    }

    TrafficLight mapToTrafficLight(Severity severity) {
        for (TrafficLight light: TrafficLight.values()) {
            if (light.getSeverities().contains(severity)) {
                return light;
            }
        }
        throw new IllegalStateException("Severity: "+severity+" is not found by any trafficlight - may not happen.");
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

    private boolean hasResultOneFindingWith(SecHubResult result, Severity severity) {
        if (severity == null) {
            return false;
        }
        List<SecHubFinding> findings = result.getFindings();
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
