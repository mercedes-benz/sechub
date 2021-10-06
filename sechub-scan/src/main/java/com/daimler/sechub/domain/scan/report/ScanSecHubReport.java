// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.commons.model.SecHubMessage;
import com.daimler.sechub.commons.model.SecHubMessageType;
import com.daimler.sechub.commons.model.SecHubReportData;
import com.daimler.sechub.commons.model.SecHubReportModel;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.SecHubStatus;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
                model.setStatus(SecHubStatus.OK);
            } catch (JSONConverterException e) {
                LOG.error("{} FATAL PROBLEM! Failed to create sechub result for origin:\n{}", UUIDTraceLogID.traceLogID(report.getSecHubJobUUID()),
                        report.getResult());
                String info = "Origin result data problems! Please inform administrators about this problem.";
                SecHubMessage message = new SecHubMessage(SecHubMessageType.ERROR,info);
                model.getMessages().add(message);

                model.getMessages().add(new SecHubMessage(SecHubMessageType.ERROR, "Internal SecHub failure happend."));
                model.setStatus(SecHubStatus.FAILED);
            }
        } else {
            throw new IllegalStateException("Unsupported report result type:" + resultType);
        }

        /* calculate data */
        buildCalculatedData(report);
    }

    private void buildCalculatedData(ScanReport report) {

        model.setTrafficLight(TrafficLight.fromString(report.getTrafficLightAsString()));
        model.getResult().setCount(model.getResult().getFindings().size());
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

}
