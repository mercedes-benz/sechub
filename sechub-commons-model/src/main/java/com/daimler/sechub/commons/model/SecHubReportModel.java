// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportModel implements SecHubReportData, JSONable<SecHubReportModel> {

    private static final SecHubReportModel IMPORTER = new SecHubReportModel();

    private UUID jobUUID;
    private TrafficLight trafficLight;

    private SecHubResult result = new SecHubResult();
    private SecHubStatus status;
    private String reportVersion;

    private Set<SecHubMessage> messages = new TreeSet<>();

    @Override
    public Set<SecHubMessage> getMessages() {
        return messages;
    }

    public void setMessages(Set<SecHubMessage> messages) {
        // we want to have always sorted messages, so we reuse our tree set and not use
        // given one
        this.messages.clear();
        this.messages.addAll(messages);
    }

    @Override
    public SecHubStatus getStatus() {
        return status;
    }

    public void setStatus(SecHubStatus status) {
        this.status = status;
    }

    @Override
    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    @Override
    public SecHubResult getResult() {
        return result;
    }

    public void setResult(SecHubResult result) {
        this.result = result;
    }

    @Override
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

    @Override
    public Class<SecHubReportModel> getJSONTargetClass() {
        return SecHubReportModel.class;
    }

    public static SecHubReportModel fromJSONString(String json) {
        return IMPORTER.fromJSON(json);
    }

    @Override
    public String getReportVersion() {
        return reportVersion;
    }

    @Override
    public void setReportVersion(String version) {
        this.reportVersion=version;
    }

}