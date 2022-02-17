// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLog;

public class FullScanData {

    public UUID sechubJobUUID;

    public List<ProjectScanLog> allScanLogs = new ArrayList<>();

    public List<ScanData> allScanData = new ArrayList<>();
}
