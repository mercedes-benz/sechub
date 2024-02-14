package com.mercedesbenz.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;

public class HTMLSecHubFindingData {

    private SecHubFinding finding;
    private List<HTMLScanResultCodeScanEntry> codeScanEntries = new ArrayList<>();

    public HTMLSecHubFindingData(SecHubFinding finding) {
        this.finding = finding;
    }

    public SecHubFinding getFinding() {
        return finding;
    }

    /**
     * @return code scan entries (if there are any, which is only possible for some
     *         scan types - e.g. codeScan, secretScan)
     */
    public List<HTMLScanResultCodeScanEntry> getCodeScanEntries() {
        return codeScanEntries;
    }

}
