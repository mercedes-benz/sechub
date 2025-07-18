// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;

public class FindingModel {

    private List<FindingNode> findings = new ArrayList<>();
    private UUID jobUUID;
    private TrafficLight trafficLight;

    public UUID getJobUUID() {
        return jobUUID;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public void setJobUUID(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    public int getFindingCount() {
        return findings.size();
    }

    public List<FindingNode> getFindings() {
        return findings;
    }

    public FindingNode getFirstFinding() {
        if (!findings.isEmpty()) {
            return findings.get(0);
        }
        return null;
    }
}
