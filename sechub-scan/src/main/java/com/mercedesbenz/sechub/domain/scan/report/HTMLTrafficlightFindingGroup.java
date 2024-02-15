package com.mercedesbenz.sechub.domain.scan.report;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

/**
 * Represents a group containing findings for one traffic light - e.g. RED
 *
 * Inside the HTML report it looks like
 *
 * <pre>
 *  -----------------------
 *  Red findings            <---HTMLTrafficlightFindingGroup
 *  -----------------------
 *
 *  CodeSCan                    <--- HTMLTrafficlightScanTypeFindingData
 *     | ID | Severity | Description
 *     |----------------------------------------
 *     | 1     Critical    xxx      <---- SecHubFinding
 *     | 11     High    xxx      <---- SecHubFinding
 *
 *  SecretSCan
 *     | ID | Severity | Description
 *     |----------------------------------------
 *     | 2      High   | yyyy <---- SecHubFinding
 *
 *
 * </pre>
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class HTMLTrafficlightFindingGroup {

    private TrafficLight trafficLight;
    private Map<ScanType, HTMLTrafficlightScanTypeFindingData> scanTypeToFindingDataMap = new TreeMap<>();

    public HTMLTrafficlightFindingGroup(TrafficLight trafficLight) {
        Objects.requireNonNull(trafficLight, "TrafficLight may not be null!");
        this.trafficLight = trafficLight;
    }

    public String getFindingHeadlineCssClass() {
        return trafficLight.name().toLowerCase() + "FindingHeadline";
    }

    public String getFindingsTableCssClass() {
        return trafficLight.name().toLowerCase() + "FindingsTable'";
    }

    public String getFindingHeadlineText() {
        return trafficLight.getText() + " findings";
    }

    public boolean hasEntries() {
        return !scanTypeToFindingDataMap.isEmpty();
    }

    void add(SecHubFinding finding) {
        if (finding == null) {
            return;
        }
        HTMLTrafficlightScanTypeFindingData scanTypeFindingData = scanTypeToFindingDataMap.computeIfAbsent(finding.getType(),
                scanType -> new HTMLTrafficlightScanTypeFindingData(scanType));

        scanTypeFindingData.addRelatedFinding(finding);
    }

    public Collection<HTMLTrafficlightScanTypeFindingData> getScanTypeFindingDataList() {
        return Collections.unmodifiableCollection(scanTypeToFindingDataMap.values());
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    @Override
    public String toString() {
        return "HTMLTrafficlightFindingGroup [" + (trafficLight != null ? "trafficLight=" + trafficLight + ", " : "")
                + (scanTypeToFindingDataMap != null ? "scanTypeToFindingDataMap=" + scanTypeToFindingDataMap : "") + "]";
    }

}
