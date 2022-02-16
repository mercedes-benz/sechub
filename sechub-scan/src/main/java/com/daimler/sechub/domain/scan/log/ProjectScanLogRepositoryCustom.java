// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

import java.util.List;

public interface ProjectScanLogRepositoryCustom {

    List<ProjectScanLogSummary> findSummaryLogsFor(String projectId);
}
