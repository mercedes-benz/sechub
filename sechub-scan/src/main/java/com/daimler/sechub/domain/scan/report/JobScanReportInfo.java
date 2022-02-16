// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data object to retrive just info which jobs have been run by this project
 *
 * @author Albert Tregnaghi
 *
 */
public class JobScanReportInfo {

    private String projectId;

    private UUID sechubJobUUID;
    private List<UUID> sechubJobList = new ArrayList<>();

    public JobScanReportInfo(String projectId, UUID sechubJobUUID, List<UUID> sechubJobList) {
        this.projectId = projectId;
        if (sechubJobList != null) {
            this.sechubJobList.addAll(sechubJobList);
        }
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }

    public List<UUID> getSechubJobList() {
        return sechubJobList;
    }

    public String getProjectId() {
        return projectId;
    }
}