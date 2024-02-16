// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

@Component
public class HTMLScanResultReportModelBuilder {

    static final String SHOW_LIGHT = "opacity: 1.0";
    static final String HIDE_LIGHT = "opacity: 0.25";

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
        HTMLCodeScanDescriptionSupport codeScanSupport = new HTMLCodeScanDescriptionSupport();
        SecHubResult result = report.getResult();

        Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries = new HashMap<>();
        List<SecHubFinding> findings = result.getFindings();
        for (SecHubFinding finding : findings) {
            codeScanEntries.put(finding.getId(), codeScanSupport.buildEntries(finding));
        }

        List<HTMLScanTypSummary> scanTypeSummaries = createScanTypeSummaries(findings);

        List<HTMLTrafficlightFindingGroup> trafficLightGroups = createTrafficLightFindingGroups(findings);

        Map<String, Object> model = new HashMap<>();
        model.put("result", report.getResult());

        model.put("trafficlight", trafficLight.name());

        model.put("styleRed", styleRed);
        model.put("styleYellow", styleYellow);
        model.put("styleGreen", styleGreen);
        model.put("codeScanEntries", codeScanEntries);
        model.put("codeScanSupport", codeScanSupport);
        model.put("scanTypeSummaries", scanTypeSummaries);
        model.put("trafficLightGroups", trafficLightGroups);
        model.put("reportHelper", HTMLReportHelper.DEFAULT);
        model.put("messages", report.getMessages());
        model.put("metaData", report.getMetaData());

        UUID jobUUID = report.getJobUUID();
        if (jobUUID != null) {
            model.put("jobuuid", jobUUID.toString());
        } else {
            model.put("jobuuid", "none");
        }

        return model;
    }

    /**
     * Creates a list with scan type summary elements for given findings - used for
     * rendering summary
     *
     * @param findings
     * @return a list with elements of {@link HTMLScanTypSummary}, sorted by scan
     *         type
     *
     */
    public List<HTMLScanTypSummary> createScanTypeSummaries(List<SecHubFinding> findings) {

        Map<ScanType, HTMLScanTypSummary> temporaryMap = new LinkedHashMap<>();

        for (SecHubFinding finding : findings) {

            ScanType type = finding.getType();
            HTMLScanTypSummary scanTypeSummary = temporaryMap.computeIfAbsent(type, scanType -> new HTMLScanTypSummary(scanType));
            scanTypeSummary.add(finding);

        }
        List<HTMLScanTypSummary> summaries = new ArrayList<>();
        summaries.addAll(temporaryMap.values());
        return summaries;
    }

    /**
     * Creates a list of traffic light finding groups - used for rendering findings
     *
     * @param findings
     * @return list
     */
    public List<HTMLTrafficlightFindingGroup> createTrafficLightFindingGroups(List<SecHubFinding> findings) {

        List<HTMLTrafficlightFindingGroup> groups = new ArrayList<>();

        /* attention: the list is also the ordering, so please keep RED,YELLOW,GREEN */
        groups.add(new HTMLTrafficlightFindingGroup(TrafficLight.RED));
        groups.add(new HTMLTrafficlightFindingGroup(TrafficLight.YELLOW));
        groups.add(new HTMLTrafficlightFindingGroup(TrafficLight.GREEN));

        /* add findings for each traffic light */
        for (HTMLTrafficlightFindingGroup group : groups) {
            TrafficLight trafficLight = group.getTrafficLight();

            for (SecHubFinding finding : findings) {
                if (trafficLight.getSeverities().contains(finding.getSeverity())) {
                    group.add(finding);
                }
            }
        }

        return groups;
    }

}
