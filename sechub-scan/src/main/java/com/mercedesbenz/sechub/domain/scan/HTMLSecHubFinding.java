package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;

public class HTMLSecHubFinding extends SecHubFinding {

    private List<HTMLScanResultCodeScanEntry> entryList = new ArrayList<>();

    public List<HTMLScanResultCodeScanEntry> getEntryList() {
        return entryList;
    }
}
