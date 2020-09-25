package com.daimler.sechub.client.java;

import java.util.UUID;

import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.TrafficLight;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown =true)
public class SecHubReport {
    private UUID jobUUID;
    private TrafficLight trafficLight;

    private SecHubResult result;

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public SecHubResult getResult() {
        return result;
    }

    public void setResult(SecHubResult result) {
        this.result = result;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    public void setJobUUID(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    @Override
    public String toString() {
        return "SecHubReport [jobUUID=" + jobUUID + ", trafficLight=" + trafficLight + ", result=" + result + "]";
    }
}
