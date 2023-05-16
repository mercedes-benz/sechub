// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class HTMLScanResultReportModelBuilder {

    static final String SHOW_LIGHT = "opacity: 1.0";
    static final String HIDE_LIGHT = "opacity: 0.25";

    private static final Logger LOG = LoggerFactory.getLogger(HTMLScanResultReportModelBuilder.class);

    @Value("${sechub.development.webdesignmode.enabled:false}")
    @MustBeDocumented(scope = "development", value = "Developers can turn on this mode to have reports creating with external css. Normally the html model builder will create embedded css content")
    boolean webDesignMode;

    @Value("classpath:templates/report/html/scanresult.css")
    Resource cssResource;

    String embeddedCSS;

    @Autowired
    SecHubResultTrafficLightFilter trafficLightFilter;

    public Map<String, Object> build(ScanSecHubReport report) {
        TrafficLight trafficLight = report.getTrafficLight();

        String styleRed = HIDE_LIGHT;
        String styleYellow = HIDE_LIGHT;
        String styleGreen = HIDE_LIGHT;

        if (trafficLight == null) {
            throw new IllegalStateException("No traffic light defined");
        }

        switch (trafficLight) {
            case RED:
                styleRed = SHOW_LIGHT;
                break;
            case YELLOW:
                styleYellow = SHOW_LIGHT;
                break;
            case GREEN:
                styleGreen = SHOW_LIGHT;
                break;
            default:
        }
        HtmlCodeScanDescriptionSupport codeScanSupport = new HtmlCodeScanDescriptionSupport();
        SecHubResult result = report.getResult();

        Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries = new HashMap<>();
        for (SecHubFinding finding : result.getFindings()) {
            codeScanEntries.put(finding.getId(), codeScanSupport.buildEntries(finding));
        }

        Map<String, Object> model = new HashMap<>();
        model.put("result", report.getResult());
        model.put("redList", trafficLightFilter.filterFindingsFor(result, TrafficLight.RED));
        model.put("yellowList", trafficLightFilter.filterFindingsFor(result, TrafficLight.YELLOW));
        model.put("greenList", trafficLightFilter.filterFindingsFor(result, TrafficLight.GREEN));

        model.put("trafficlight", trafficLight.name());

        model.put("styleRed", styleRed);
        model.put("styleYellow", styleYellow);
        model.put("styleGreen", styleGreen);
        model.put("isWebDesignMode", webDesignMode);
        model.put("codeScanEntries", codeScanEntries);
        model.put("codeScanSupport", codeScanSupport);
        model.put("reportHelper", HTMLReportHelper.DEFAULT);
        model.put("messages", report.getMessages());
        model.put("metaData", report.getMetaData());

        if (webDesignMode) {
            File file;
            try {
                if (cssResource == null) {
                    LOG.error("CSS resource not set:{}", cssResource);
                } else {
                    file = cssResource.getFile();
                    String absolutePathToCSSFile = file.getAbsolutePath();
                    LOG.info("Web design mode activate, using not embedded css but ref to:{}", absolutePathToCSSFile);
                    model.put("includedCSSRef", absolutePathToCSSFile);
                }
            } catch (Exception e) {
                LOG.error("Was not able get file from resource:{}", cssResource, e);
            }
        }
        UUID jobUUID = report.getJobUUID();
        if (jobUUID != null) {
            model.put("jobuuid", jobUUID.toString());
        } else {
            model.put("jobuuid", "none");
        }

        Map<ScanType, ScanTypeCount> scanSummaryMap = new HashMap<>();
        for (SecHubFinding finding : result.getFindings()) {
            ScanType scanType = finding.getType();
            ScanTypeCount scanTypeCount;
            if (scanSummaryMap.containsKey(scanType)) {
                scanTypeCount = scanSummaryMap.get(scanType);
            } else {
                scanTypeCount = ScanTypeCount.of(scanType);
                scanSummaryMap.put(scanType, scanTypeCount);
            }
            incrementScanCount(finding.getSeverity(), scanTypeCount);
        }
        Set<ScanTypeCount> scanTypeCountSet = new TreeSet<>();
        scanTypeCountSet.addAll(scanSummaryMap.values());
        model.put("scanTypeCountSet", scanTypeCountSet);

        return model;
    }

    protected void incrementScanCount(Severity severity, ScanTypeCount scanTypeCount) {
        if (Severity.HIGH.equals(severity)) {
            scanTypeCount.incrementHighSeverityCount();
        }
        if (Severity.MEDIUM.equals(severity)) {
            scanTypeCount.incrementMediumSeverityCount();
        }
        if (Severity.LOW.equals(severity)) {
            scanTypeCount.incrementLowSeverityCount();
        }
    }

}
