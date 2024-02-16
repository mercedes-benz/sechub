package com.mercedesbenz.sechub.domain.scan.report;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.ScanTypeSummaryDetailData;
import com.mercedesbenz.sechub.commons.model.ScanTypeSummaryFindingOverviewData;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubReportScanTypeSummary;
import com.mercedesbenz.sechub.commons.model.SecHubReportSummary;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;

/**
 * This class contains all logic to transform a scan report (without summary
 * data) to a report model containing summaries
 *
 */
public class ScanReportToSecHubReportModelWithSummariesTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ScanReportToSecHubReportModelWithSummariesTransformer.class);

    public ScanReportToSecHubReportModelWithSummariesTransformer() {

    }

    private class Context {
        private SecHubReportModel model;

        private Map<ScanType, ScanTypeFindingOverviewContainer> overviewContainerMap = new HashMap<>();

        public ScanReport report;

        private Context() {
            for (ScanType scantype : ScanType.values()) {
                overviewContainerMap.put(scantype, new ScanTypeFindingOverviewContainer(scantype));
            }
        }
    }

    public SecHubReportModel transform(ScanReport report) {
        notNull(report, "Report may not be null!");

        Context context = new Context();
        context.report = report;

        ScanReportResultType resultType = report.getResultType();
        if (resultType == null) {
            resultType = ScanReportResultType.RESULT;
            LOG.warn("In scan report for job:{} was no result type set, fallback set to:{}", report.getSecHubJobUUID(), resultType);
        }
        if (ScanReportResultType.MODEL.equals(resultType)) {
            try {
                context.model = SecHubReportModel.fromJSONString(report.getResult());
                if (context.model.getJobUUID() == null) {
                    // Fallback for problems when model did not contain job uuid - see
                    // https://github.com/mercedes-benz/sechub/issues/864
                    LOG.warn("Job uuid not found inside report result JSON, will set Job UUID from entity data");
                    context.model.setJobUUID(report.getSecHubJobUUID());
                }
            } catch (JSONConverterException e) {
                LOG.error("FATAL PROBLEM! Failed to create sechub result by model for job:{}", report.getSecHubJobUUID(), e);

                context.model.getMessages().add(new SecHubMessage(SecHubMessageType.ERROR, "Internal SecHub failure happend."));
                context.model.setJobUUID(report.getSecHubJobUUID());
                context.model.setStatus(SecHubStatus.FAILED);
            }
        } else if (ScanReportResultType.RESULT.equals(resultType)) {
            LOG.debug("Found old report result, will create artificial model");

            context.model = new SecHubReportModel();
            context.model.setJobUUID(report.getSecHubJobUUID());

            try {
                context.model.setResult(SecHubResult.fromJSONString(report.getResult()));
                context.model.setStatus(SecHubStatus.SUCCESS);

            } catch (JSONConverterException e) {
                LOG.error("{} FATAL PROBLEM! Failed to set sechub result because of JSON conversion problems. Tried to convert:\n{}",
                        UUIDTraceLogID.traceLogID(report.getSecHubJobUUID()), report.getResult(), e);

                String info = "Origin result data problems! Please inform administrators about this problem.";
                SecHubMessage message = new SecHubMessage(SecHubMessageType.ERROR, info);
                context.model.getMessages().add(message);

                context.model.getMessages().add(new SecHubMessage(SecHubMessageType.ERROR, "Internal SecHub failure happend."));
                context.model.setStatus(SecHubStatus.FAILED);
            }
        } else {
            throw new IllegalStateException("Unsupported report result type:" + resultType);
        }

        SecHubReportMetaData reportMetaData = new SecHubReportMetaData();
        context.model.setMetaData(reportMetaData);

        /* calculate data */
        buildCalculatedData(context);

        return context.model;
    }

    private void buildCalculatedData(Context context) {
        context.model.setTrafficLight(TrafficLight.fromString(context.report.getTrafficLightAsString()));
        context.model.getResult().setCount(context.model.getResult().getFindings().size());

        calculateSummary(context);
    }

    /**
     * Internal object while calculation is running - works as a bridge between scan
     * type and summary until calculation is done. Not part of the JSON result.
     *
     * @author Albert Tregnaghi
     *
     */
    private class ScanTypeSummaryCalculationData {
        private SecHubReportScanTypeSummary summary;
        private ScanType scanType;

        public ScanTypeSummaryCalculationData(ScanType scanType, SecHubReportScanTypeSummary summary) {
            super();
            this.scanType = scanType;
            this.summary = summary;
        }

        public ScanType getScanType() {
            return scanType;
        }

        public SecHubReportScanTypeSummary getSummary() {
            return summary;
        }
    }

    protected void calculateSummary(Context context) {

        Map<ScanType, ScanTypeSummaryCalculationData> calculationMap = createInitialCalculationMap();

        List<SecHubFinding> findings = context.model.getResult().getFindings();

        for (SecHubFinding finding : findings) {
            ScanType scanType = finding.getType();
            if (scanType == null) {
                LOG.warn("Finding: {} has no scan type!", finding);
                continue;
            }
            ScanTypeSummaryCalculationData scanTypeSummaryCalculationData = calculationMap.get(scanType);
            if (scanTypeSummaryCalculationData == null) {
                continue;
            }
            add(context, scanTypeSummaryCalculationData.getSummary(), finding);
        }

        calculateTotals(calculationMap.values());
        calculateOverviewData(context, calculationMap.values());

        /*
         * we always replace the summary - means if there was a summary before, it is
         * new clean calculated here
         */
        SecHubReportSummary summary = createSummaryContainingOnlyScanTypeDataWithTotalNotZero(calculationMap);
        SecHubReportMetaData metaData = ensureMetaDataInModel(context);
        metaData.setSummary(summary);
    }

    private Map<ScanType, ScanTypeSummaryCalculationData> createInitialCalculationMap() {
        Map<ScanType, ScanTypeSummaryCalculationData> tempScanTypeToCalculationDataMap = new LinkedHashMap<>(6);
        for (ScanType scanType : ScanType.values()) {
            if (scanType.isInternalScanType()) {
                continue;
            }
            initForCalculation(tempScanTypeToCalculationDataMap, scanType);
        }
        return tempScanTypeToCalculationDataMap;
    }

    private SecHubReportSummary createSummaryContainingOnlyScanTypeDataWithTotalNotZero(
            Map<ScanType, ScanTypeSummaryCalculationData> tempScanTypeToCalculationMap) {
        SecHubReportSummary summary = new SecHubReportSummary();

        for (ScanType scanType : tempScanTypeToCalculationMap.keySet()) {
            ScanTypeSummaryCalculationData data = tempScanTypeToCalculationMap.get(scanType);
            if (data == null) {
                continue;
            }
            SecHubReportScanTypeSummary scanTypeSummary = data.getSummary();
            if (scanTypeSummary.getTotal() == 0) {
                /*
                 * means we have only empty entries - in this case we keep the origin data
                 * (which is initially Optional.ofNullable(null) )
                 */
                continue;
            }

            switch (scanType) {
            case CODE_SCAN:
                summary.setCodeScan(scanTypeSummary);
                break;
            case INFRA_SCAN:
                summary.setInfraScan(scanTypeSummary);
                break;
            case LICENSE_SCAN:
                summary.setLicenseScan(scanTypeSummary);
                break;
            case SECRET_SCAN:
                summary.setSecretScan(scanTypeSummary);
                break;
            case WEB_SCAN:
                summary.setWebScan(scanTypeSummary);
                break;
            case UNKNOWN:
            case REPORT:
            case ANALYTICS:
            default:
                /* sanity check */
                if (!scanType.isInternalScanType()) {
                    throw new IllegalStateException("The non internal scan type: " + scanType + " is not handled.");
                }
                break;
            }
        }
        return summary;
    }

    private SecHubReportMetaData ensureMetaDataInModel(Context context) {
        Optional<SecHubReportMetaData> metaDataOpt = context.model.getMetaData();

        SecHubReportMetaData metaData = null;
        if (metaDataOpt.isPresent()) {
            metaData = metaDataOpt.get();
        } else {
            metaData = new SecHubReportMetaData();
            context.model.setMetaData(metaData);
        }
        return metaData;
    }

    private void initForCalculation(Map<ScanType, ScanTypeSummaryCalculationData> map, ScanType scanType) {
        map.put(scanType, new ScanTypeSummaryCalculationData(scanType, new SecHubReportScanTypeSummary()));

    }

    private void calculateTotals(Collection<ScanTypeSummaryCalculationData> scanTypeSummaryCalculationDatas) {
        for (ScanTypeSummaryCalculationData scanTypeSummaryCalculationData : scanTypeSummaryCalculationDatas) {
            SecHubReportScanTypeSummary summary = scanTypeSummaryCalculationData.getSummary();

            int total = 0;

            total += summary.getCritical();
            total += summary.getHigh();
            total += summary.getMedium();
            total += summary.getLow();
            total += summary.getUnclassified();
            total += summary.getInfo();

            summary.setTotal(total);
        }
    }

    private void calculateOverviewData(Context context, Collection<ScanTypeSummaryCalculationData> scanTypeSummaryCalculationDatas) {
        for (ScanTypeSummaryCalculationData scanTypeSummaryCalculationData : scanTypeSummaryCalculationDatas) {

            ScanType scanType = scanTypeSummaryCalculationData.getScanType();
            ScanTypeFindingOverviewContainer overviewContainer = context.overviewContainerMap.get(scanType);

            for (Severity severity : Severity.values()) {
                Map<String, ScanTypeSummaryFindingOverviewData> map = overviewContainer.getMapForSeverity(severity);
                Collection<ScanTypeSummaryFindingOverviewData> overviewDataEntries = map.values();
                if (overviewDataEntries.isEmpty()) {
                    continue;
                }
                SecHubReportScanTypeSummary summary = scanTypeSummaryCalculationData.getSummary();
                ScanTypeSummaryDetailData details = summary.getDetails();

                switch (severity) {
                case CRITICAL -> details.getCritical().addAll(overviewDataEntries);
                case HIGH -> details.getHigh().addAll(overviewDataEntries);
                case INFO -> details.getInfo().addAll(overviewDataEntries);
                case LOW -> details.getLow().addAll(overviewDataEntries);
                case MEDIUM -> details.getMedium().addAll(overviewDataEntries);
                case UNCLASSIFIED -> details.getUnclassified().addAll(overviewDataEntries);
                default -> throw new IllegalStateException("Unhandled severity: " + severity);
                }
            }
        }
    }

    protected void add(Context context, SecHubReportScanTypeSummary scanTypeSummary, SecHubFinding finding) {
        incrementSummaryCounts(scanTypeSummary, finding);
        addToOverview(context, finding);
    }

    private void incrementSummaryCounts(SecHubReportScanTypeSummary scanTypeSummary, SecHubFinding finding) {
        Severity severity = finding.getSeverity();

        switch (severity) {
        case CRITICAL -> scanTypeSummary.incrementCritical();
        case HIGH -> scanTypeSummary.incrementHigh();
        case MEDIUM -> scanTypeSummary.incrementMedium();
        case LOW -> scanTypeSummary.incrementLow();
        case UNCLASSIFIED -> scanTypeSummary.incrementUnclassified();
        case INFO -> scanTypeSummary.incrementInfo();
        }
    }

    /*
     * Adds given finding to calculation data. Be aware: there is no duplication
     * check
     */
    private void addToOverview(Context context, SecHubFinding finding) {
        Map<String, ScanTypeSummaryFindingOverviewData> map = fetchOverviewContainerMapForScanTypeAndSeverity(context, finding);

        Integer cweId = finding.getCweId();
        String name = finding.getName() != null ? finding.getName() : "no_name";

        ScanTypeSummaryFindingOverviewData summaryDetailData = map.computeIfAbsent(name, key -> new ScanTypeSummaryFindingOverviewData(cweId, name));

        summaryDetailData.incrementCount();
    }

    private Map<String, ScanTypeSummaryFindingOverviewData> fetchOverviewContainerMapForScanTypeAndSeverity(Context context, SecHubFinding finding) {
        Severity severity = finding.getSeverity();
        ScanTypeFindingOverviewContainer scanTypeFindingOverviewContainer = fetchOverviewContainer(context, finding.getType());

        return scanTypeFindingOverviewContainer.getMapForSeverity(severity);
    }

    private ScanTypeFindingOverviewContainer fetchOverviewContainer(Context context, ScanType type) {
        return context.overviewContainerMap.computeIfAbsent(type, key -> new ScanTypeFindingOverviewContainer(type));
    }

}
