package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;

public class HTMLWebSecHubFinding extends SecHubFinding {

    private List<SecHubFinding> entryList = new ArrayList<>();

    public List<SecHubFinding> getEntryList() {
        return entryList;
    }
}
