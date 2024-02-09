// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubReportData;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

@JsonIgnoreProperties(ignoreUnknown = true)
@MustBeKeptStable("This is the result returend from REST API to cli and other systems. So has to be stable")
public class ScanSecHubReport implements SecHubReportData, JSONable<ScanSecHubReport> {

    public static final String PROPERTY_RESULT = "result";
    public static final String PROPERTY_JOBUUID = "jobUUID";
    public static final String PROPERTY_TRAFFICLIGHT = "trafficLight";
    public static final String PROPERTY_INFO = "info";

    private static final ScanSecHubReport IMPORTER = new ScanSecHubReport();

    private SecHubReportModel model;

    private ScanSecHubReport() {
        /* only internal for IMPORTER */
        model = new SecHubReportModel();
    }

    /**
     * Creates a scan report result by given scan report.
     *
     * @param report
     */
    public ScanSecHubReport(ScanReport report) {
        this.model = new ScanReportToSecHubReportModelTransformer().transform(report);
    }

    @Override
    public Class<ScanSecHubReport> getJSONTargetClass() {
        return ScanSecHubReport.class;
    }

    public static ScanSecHubReport fromJSONString(String json) {
        return IMPORTER.fromJSON(json);
    }

    @Override
    public Set<SecHubMessage> getMessages() {
        return model.getMessages();
    }

    @Override
    public SecHubStatus getStatus() {
        return model.getStatus();
    }

    @Override
    public void setMessages(Set<SecHubMessage> messages) {
        model.setMessages(messages);
    }

    @Override
    public void setStatus(SecHubStatus status) {
        model.setStatus(status);
    }

    @Override
    public void setTrafficLight(TrafficLight trafficLight) {
        model.setTrafficLight(trafficLight);
    }

    @Override
    public void setResult(SecHubResult result) {
        model.setResult(result);
    }

    @Override
    public void setJobUUID(UUID jobUUID) {
        model.setJobUUID(jobUUID);
    }

    @Override
    public UUID getJobUUID() {
        return model.getJobUUID();
    }

    @Override
    public SecHubResult getResult() {
        return model.getResult();
    }

    @Override
    public TrafficLight getTrafficLight() {
        return model.getTrafficLight();
    }

    @Override
    public String getReportVersion() {
        return model.getReportVersion();
    }

    @Override
    public void setReportVersion(String version) {
        model.setReportVersion(version);
    }

    @Override
    public Optional<SecHubReportMetaData> getMetaData() {
        return model.getMetaData();
    }

    @Override
    public void setMetaData(SecHubReportMetaData metaData) {
        model.setMetaData(metaData);
    }

}
