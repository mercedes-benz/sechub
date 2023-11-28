// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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

        model.put("scanTypeCountSet", prepareScanTypesForModel(result.getFindings()));

        model.put("redHTMLSecHubFindingList", filterFindingsForGeneralScan(result.getFindings(), codeScanEntries, List.of(Severity.HIGH)));
        model.put("yellowHTMLSecHubFindingList", filterFindingsForGeneralScan(result.getFindings(), codeScanEntries, List.of(Severity.MEDIUM)));
        model.put("greenHTMLSecHubFindingList", filterFindingsForGeneralScan(result.getFindings(), codeScanEntries, List.of(Severity.INFO, Severity.LOW)));

        model.put("redHTMLWebScanMap", filterFindingsForWebScan(result.getFindings(), List.of(Severity.HIGH)));
        model.put("yellowHTMLWebScanMap", filterFindingsForWebScan(result.getFindings(), List.of(Severity.MEDIUM)));
        model.put("greenHTMLWebScanMap", filterFindingsForWebScan(result.getFindings(), List.of(Severity.INFO, Severity.LOW)));

        return model;
    }

    protected Set<ScanTypeCount> prepareScanTypesForModel(List<SecHubFinding> findings) {
        Map<ScanType, ScanTypeCount> scanSummaryMap = new HashMap<>();
        for (SecHubFinding finding : findings) {
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
        return scanTypeCountSet;
    }

    protected void incrementScanCount(Severity severity, ScanTypeCount scanTypeCount) {
        switch (severity) {
        case HIGH, CRITICAL -> scanTypeCount.incrementHighSeverityCount();
        case MEDIUM -> scanTypeCount.incrementMediumSeverityCount();
        case UNCLASSIFIED, INFO, LOW -> scanTypeCount.incrementLowSeverityCount();
        }
    }

    public Map<String, List<SecHubFinding>> filterFindingsForWebScan(List<SecHubFinding> findings, List<Severity> severities) {
        Map<String, List<SecHubFinding>> groupedFindingsByName = findings.stream().filter(finding -> severities.contains(finding.getSeverity()))
                .filter(finding -> finding.hasScanType(ScanType.WEB_SCAN.getId())).collect(groupingBy(SecHubFinding::getName));
        Map<String, List<SecHubFinding>> groupedAndSortedFindingsByName = new TreeMap<>();
        groupedAndSortedFindingsByName.putAll(groupedFindingsByName);
        return groupedAndSortedFindingsByName;
    }

    public List<HTMLSecHubFinding> filterFindingsForGeneralScan(List<SecHubFinding> findings, Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries,
            List<Severity> severitiesToShow) {
        List<HTMLSecHubFinding> htmlSecHubFindings = new LinkedList<>();
        Map<String, List<SecHubFinding>> groupedFindingsByName = findings.stream().filter(finding -> severitiesToShow.contains(finding.getSeverity()))
                .collect(groupingBy(SecHubFinding::getName));

        Map<String, List<SecHubFinding>> groupedAndSortedFindingsByName = new TreeMap<>();
        groupedAndSortedFindingsByName.putAll(groupedFindingsByName);

        groupedAndSortedFindingsByName.entrySet().stream().forEach(entry -> {
            List<SecHubFinding> findingList = entry.getValue();
            if (!findingList.isEmpty()) {
                SecHubFinding firstFinding = findingList.get(0);
                HTMLSecHubFinding htmlSecHubFinding = new HTMLSecHubFinding();
                BeanUtils.copyProperties(firstFinding, htmlSecHubFinding);
                htmlSecHubFinding.setId(0);
                List<HTMLScanResultCodeScanEntry> entryList = htmlSecHubFinding.getEntryList();
                for (SecHubFinding finding : findingList) {
                    if (!finding.hasScanType(ScanType.WEB_SCAN.getId())) {
                        List<HTMLScanResultCodeScanEntry> codeScanEntryList = codeScanEntries.get(finding.getId());
                        for (HTMLScanResultCodeScanEntry htmlScanResultCodeScanEntry : codeScanEntryList) {
                            entryList.add(htmlScanResultCodeScanEntry);
                        }
                    }
                }
                htmlSecHubFindings.add(htmlSecHubFinding);
            }
        });
        return htmlSecHubFindings;
    }
}
