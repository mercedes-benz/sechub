// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;

@JsonIgnoreProperties(ignoreUnknown = true)
@MustBeKeptStable("This is the result returend from REST API to cli and other systems. So has to be stable")
public class ScanSecHubReport implements SecHubReportData, JSONable<ScanSecHubReport> {

    public static final String PROPERTY_RESULT = "result";
    public static final String PROPERTY_JOBUUID = "jobUUID";
    public static final String PROPERTY_TRAFFICLIGHT = "trafficLight";
    public static final String PROPERTY_INFO = "info";

    private static final Logger LOG = LoggerFactory.getLogger(ScanSecHubReport.class);

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
        notNull(report, "Report may not be null!");

        ScanReportResultType resultType = report.getResultType();
        if (resultType == null) {
            resultType = ScanReportResultType.RESULT;
            LOG.warn("In scan report for job:{} was no result type set, fallback set to:{}", report.getSecHubJobUUID(), resultType);
        }

        if (ScanReportResultType.MODEL.equals(resultType)) {
            try {
                model = SecHubReportModel.fromJSONString(report.getResult());
                if (model.getJobUUID() == null) {
                    // Fallback for problems when model did not contain job uuid - see
                    // https://github.com/mercedes-benz/sechub/issues/864
                    LOG.warn("Job uuid not found inside report result JSON, will set Job UUID from entity data");
                    model.setJobUUID(report.getSecHubJobUUID());
                }
            } catch (JSONConverterException e) {
                LOG.error("FATAL PROBLEM! Failed to create sechub result by model for job:{}", report.getSecHubJobUUID(), e);

                model.getMessages().add(new SecHubMessage(SecHubMessageType.ERROR, "Internal SecHub failure happend."));
                model.setJobUUID(report.getSecHubJobUUID());
                model.setStatus(SecHubStatus.FAILED);
            }
        } else if (ScanReportResultType.RESULT.equals(resultType)) {
            LOG.debug("Found old report result, will create artificial model");

            model = new SecHubReportModel();
            model.setJobUUID(report.getSecHubJobUUID());

            try {
                model.setResult(SecHubResult.fromJSONString(report.getResult()));
                model.setStatus(SecHubStatus.SUCCESS);

            } catch (JSONConverterException e) {
                LOG.error("{} FATAL PROBLEM! Failed to set sechub result because of JSON conversion problems. Tried to convert:\n{}",
                        UUIDTraceLogID.traceLogID(report.getSecHubJobUUID()), report.getResult(), e);

                String info = "Origin result data problems! Please inform administrators about this problem.";
                SecHubMessage message = new SecHubMessage(SecHubMessageType.ERROR, info);
                model.getMessages().add(message);

                model.getMessages().add(new SecHubMessage(SecHubMessageType.ERROR, "Internal SecHub failure happend."));
                model.setStatus(SecHubStatus.FAILED);
            }
        } else {
            throw new IllegalStateException("Unsupported report result type:" + resultType);
        }

        SecHubReportMetaData reportMetaData = new SecHubReportMetaData();
        setMetaData(reportMetaData);

        SecHubReportSummary secHubReportSummary = new SecHubReportSummary();
        reportMetaData.setSummary(secHubReportSummary);

        /* calculate data */
        buildCalculatedData(report);
    }

    private void buildCalculatedData(ScanReport report) {
        model.setTrafficLight(TrafficLight.fromString(report.getTrafficLightAsString()));
        model.getResult().setCount(model.getResult().getFindings().size());
        calculateSummary();
    }

    protected void calculateSummary() {
        var summary = model.getMetaData().get().getSummary();
        SecHubReportMetaDataSummary codeScan = summary.getCodeScan();
        SecHubReportMetaDataSummary infraScan = summary.getInfraScan();
        SecHubReportMetaDataSummary licenseScan = summary.getLicenseScan();
        SecHubReportMetaDataSummary secretScan = summary.getSecretScan();
        SecHubReportMetaDataSummary webScan = summary.getWebScan();

        for (SecHubFinding finding : model.getResult().getFindings()) {
            ScanType scanType = finding.getType();
            if (scanType != null) {
                switch (scanType) {
                case CODE_SCAN -> codeScan.reportScanHelper(finding);
                case INFRA_SCAN -> infraScan.reportScanHelper(finding);
                case WEB_SCAN -> webScan.reportScanHelper(finding);
                case LICENSE_SCAN -> licenseScan.reportScanHelper(finding);
                case SECRET_SCAN -> secretScan.reportScanHelper(finding);
                }
            }
        }
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
