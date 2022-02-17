// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.SecHubReportProductTransformerService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsJobReport;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class DownloadScanReportService {

    @Autowired
    ScanAssertService scanAssertService;

    @Autowired
    SecHubReportProductTransformerService secHubResultService;

    @Autowired
    ScanReportRepository reportRepository;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseUserDownloadsJobReport(@Step(number = 3, name = "Resolve scan report result"))
    public ScanSecHubReport getScanSecHubReport(String projectId, UUID jobUUID) {
        /* validate */
        assertion.isValidProjectId(projectId);
        assertion.isValidJobUUID(jobUUID);

        scanAssertService.assertUserHasAccessToProject(projectId);
        scanAssertService.assertProjectAllowsReadAccess(projectId);

        /* audit */
        auditLogService.log("starts download of report for job: {}", jobUUID);

        ScanReport report = reportRepository.findBySecHubJobUUID(jobUUID);

        if (report == null) {
            throw new NotFoundException("Report not found or you have no access to report!");
        }
        scanAssertService.assertUserHasAccessToReport(report);

        return new ScanSecHubReport(report);
    }

}
