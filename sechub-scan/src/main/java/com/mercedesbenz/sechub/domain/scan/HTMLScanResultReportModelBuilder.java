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

        Map<Integer, List<HTMLScanResultCodeScanEntry>> findingIdToCodeScanEntriesMap = new HashMap<>();
        for (SecHubFinding finding : result.getFindings()) {
            findingIdToCodeScanEntriesMap.put(finding.getId(), codeScanSupport.buildEntries(finding));
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
        model.put("codeScanEntries", findingIdToCodeScanEntriesMap);
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

        /* summary data for first table row */
        model.put("scanTypeCountSet", createScanTypeCountSet(result.getFindings()));

        /* detail data : */
        model.put("redHTMLSecHubFindingList",
                createCodeScanDataList(result.getFindings(), findingIdToCodeScanEntriesMap, List.of(Severity.CRITICAL, Severity.HIGH)));
        model.put("yellowHTMLSecHubFindingList", createCodeScanDataList(result.getFindings(), findingIdToCodeScanEntriesMap, List.of(Severity.MEDIUM)));
        model.put("greenHTMLSecHubFindingList",
                createCodeScanDataList(result.getFindings(), findingIdToCodeScanEntriesMap, List.of(Severity.LOW, Severity.UNCLASSIFIED, Severity.INFO)));

        model.put("redHTMLWebScanMap", createWebScanDataForSeverity(result.getFindings(), List.of(Severity.HIGH)));
        model.put("yellowHTMLWebScanMap", createWebScanDataForSeverity(result.getFindings(), List.of(Severity.MEDIUM)));
        model.put("greenHTMLWebScanMap", createWebScanDataForSeverity(result.getFindings(), List.of(Severity.LOW, Severity.UNCLASSIFIED, Severity.INFO)));

        return model;
    }

    protected Set<ScanTypeCount> createScanTypeCountSet(List<SecHubFinding> findings) {

        /* For calculation we use a map: */
        Map<ScanType, ScanTypeCount> scanTypeToCountMap = new HashMap<>();
        for (SecHubFinding finding : findings) {

            ScanType scanType = finding.getType();
            ScanTypeCount scanTypeCount;

            if (scanTypeToCountMap.containsKey(scanType)) {
                scanTypeCount = scanTypeToCountMap.get(scanType);
            } else {
                scanTypeCount = ScanTypeCount.of(scanType);
                scanTypeToCountMap.put(scanType, scanTypeCount);
            }
            incrementScanCount(finding.getSeverity(), scanTypeCount);
        }

        /* we just need a sorted list of the count entries */
        Set<ScanTypeCount> scanTypeCountSet = new TreeSet<>(scanTypeToCountMap.values());
        return scanTypeCountSet;
    }

    protected void incrementScanCount(Severity severity, ScanTypeCount scanTypeCount) {
        scanTypeCount.increment(severity);
    }

    Map<String, List<SecHubFinding>> createWebScanDataForSeverity(List<SecHubFinding> findings, List<Severity> severitiesToShow) {
        /* @formatter:off */
        Map<String, List<SecHubFinding>> groupedFindingsByName = 
             findings.stream()
                .filter(finding -> severitiesToShow.contains(finding.getSeverity()))
                .filter(finding -> finding.hasScanType(ScanType.WEB_SCAN.getId()))
                .collect(groupingBy(SecHubFinding::getName));
        
        /* @formatter:on */
        Map<String, List<SecHubFinding>> groupedAndSortedFindingsByName = new TreeMap<>(groupedFindingsByName);

        return groupedAndSortedFindingsByName;
    }

    List<HTMLCodeScanEntriesSecHubFindingData> createCodeScanDataList(List<SecHubFinding> findings, Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries,
            List<Severity> severitiesToShow) {

        List<HTMLCodeScanEntriesSecHubFindingData> htmlSecHubFindings = new LinkedList<>();
        /* @formatter:off */
        Map<String, List<SecHubFinding>> groupedFindingsByName = 
             findings.stream()
                .filter(finding -> severitiesToShow.contains(finding.getSeverity()))
                .collect(groupingBy(SecHubFinding::getName));
        /* @formatter:on */

        Map<String, List<SecHubFinding>> groupedAndSortedFindingsByName = new TreeMap<>(groupedFindingsByName);

        for (List<SecHubFinding> findingList : groupedAndSortedFindingsByName.values()) {
            if (findingList.isEmpty()) {
                continue;
            }
            SecHubFinding firstFinding = findingList.get(0);

            HTMLCodeScanEntriesSecHubFindingData htmlSecHubFinding = new HTMLCodeScanEntriesSecHubFindingData();
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
        return htmlSecHubFindings;
    }
}
