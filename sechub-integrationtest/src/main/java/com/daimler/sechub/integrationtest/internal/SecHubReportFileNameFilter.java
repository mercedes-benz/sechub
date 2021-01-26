// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

public class SecHubReportFileNameFilter implements FilenameFilter {

    private String searchEnd;
    private String searchBegin;

    public SecHubReportFileNameFilter(UUID uuid) {
        this(null, uuid);
    }

    public SecHubReportFileNameFilter(String projectId, UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid must be set, but is null!");
        }
        this.searchEnd = "_"+uuid.toString()+".json";

        this.searchBegin = "sechub_report_";
        
        if (projectId != null) {
            this.searchBegin += projectId;
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        if (!name.startsWith(searchBegin)) {
           return false; 
        }
        boolean endsWith = name.endsWith(searchEnd);
        return endsWith;
    }

}