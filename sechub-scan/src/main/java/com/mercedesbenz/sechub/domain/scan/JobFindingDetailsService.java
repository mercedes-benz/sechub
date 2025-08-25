// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.domain.scan.report.DownloadScanReportService;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobFinding;

@Service
public class JobFindingDetailsService {

    private DownloadScanReportService scanReportDownloadService;

    JobFindingDetailsService(DownloadScanReportService scanReportDownloadService) {
        this.scanReportDownloadService = scanReportDownloadService;
    }

    public JobFinding findDetails(String projectId, UUID jobUUID, int findingId) {

        ScanSecHubReport report = scanReportDownloadService.getObfuscatedScanSecHubReport(projectId, jobUUID);

        JobFinding finding = new JobFinding();
        finding.setJobUUID(jobUUID);
        finding.setFindingId(findingId);
        finding.setProjectId(projectId);

        SecHubFinding found = null;
        for (SecHubFinding reportFinding : report.getResult().getFindings()) {
            if (reportFinding.getId() == findingId) {
                found = reportFinding;
                break;
            }
        }
        if (found == null) {
            finding.setAvailable(false);
            return finding;
        }

        /* set result data */
        finding.setAvailable(true);
        finding.setCweId(found.getCweId());
        if (found.getPath() != null) {
            finding.setFileName(found.getPath());
        } else {
            finding.setFileName("");
        }
        finding.setRelevantSource(findRelevantpart(found.getCode()));
        finding.setFindingName(found.getName());
        finding.setFindingDescription(found.getDescription());

        return finding;
    }

    String findRelevantpart(SecHubCodeCallStack callStack) {
        if (callStack == null) {
            return "";
        }
        SecHubCodeCallStack child = callStack.getCalls();
        if (child != null) {
            return findRelevantpart(child);
        }
        return callStack.getRelevantPart();
    }

}
