package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;

/**
 * This class represents finding data with additional code entry data.
 * Currently it extends SecHubFinding.
 */
public class HTMLCodeScanEntriesSecHubFindingData extends SecHubFinding {

    /*
     * TODO Albert Tregnaghi, 2024-02-09: it is not so good, that this class
     * inherits from SecHubFinding - it is only a data representation. We should
     * avoid that somebody thinks this is really a finding of the model (and e.g.
     * tries to create a json for it, store it etc.). The right approach would be to
     * use composition here and create field. But this would also need massive
     * changes inside the templates, so it will be done later.
     */

    private List<HTMLScanResultCodeScanEntry> entryList = new ArrayList<>();

    public List<HTMLScanResultCodeScanEntry> getEntryList() {
        return entryList;
    }
}
