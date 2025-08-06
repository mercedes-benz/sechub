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

    @Autowired
    private ScanReportSensitiveDataObfuscator scanReportSensitiveDataObfuscator;

    @UseCaseUserDownloadsJobReport(@Step(number = 3, name = "Resolve scan report result"))
    public ScanSecHubReport getObfuscatedScanSecHubReport(String projectId, UUID jobUUID) {
        /* validate */
        assertion.assertIsValidProjectId(projectId);
        assertion.assertIsValidJobUUID(jobUUID);

        scanAssertService.assertUserHasAccessToProject(projectId);
        scanAssertService.assertProjectAllowsReadAccess(projectId);

        /* audit */
        auditLogService.log("starts download of report for job: {}", jobUUID);

        ScanReport report = reportRepository.findBySecHubJobUUID(jobUUID);

        if (report == null) {
            throw new NotFoundException("Report not found or you have no access to report!");
        }

        if (!projectId.equals(report.getProjectId())) {
            throw new NotFoundException("Job is not for the given project!");
        }

        scanAssertService.assertUserHasAccessToReport(report);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(report);
        scanReportSensitiveDataObfuscator.obfuscate(scanSecHubReport);

        return scanSecHubReport;
    }

}
