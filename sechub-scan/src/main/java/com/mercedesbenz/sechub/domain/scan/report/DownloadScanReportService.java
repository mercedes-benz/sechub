// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.Optional;
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

    // TODO: Write tests for this method

    public ScanSecHubReport getLatestObfuscatedScanSecHubReport(String projectId) {
        assertion.assertIsValidProjectId(projectId);

        scanAssertService.assertUserHasAccessToProject(projectId);
        scanAssertService.assertProjectAllowsReadAccess(projectId);

        Optional<UUID> optJobUUID = reportRepository.findLatestSecHubJobUUIDByProjectId(projectId);

        if (optJobUUID.isEmpty()) {
            throw new NotFoundException("Report not found or you have no access to report!");
        }

        return internalGetObfuscatedScanSecHubReport(optJobUUID.get());
    }

    @UseCaseUserDownloadsJobReport(@Step(number = 3, name = "Resolve scan report result"))
    public ScanSecHubReport getObfuscatedScanSecHubReport(String projectId, UUID jobUUID) {
        /* validate */
        assertion.assertIsValidProjectId(projectId);

        scanAssertService.assertUserHasAccessToProject(projectId);
        scanAssertService.assertProjectAllowsReadAccess(projectId);

        return internalGetObfuscatedScanSecHubReport(jobUUID);
    }

    private ScanSecHubReport internalGetObfuscatedScanSecHubReport(UUID jobUUID) {
        assertion.assertIsValidJobUUID(jobUUID);

        /* audit */
        auditLogService.log("starts download of report for job: {}", jobUUID);

        ScanReport report = reportRepository.findBySecHubJobUUID(jobUUID);

        if (report == null) {
            throw new NotFoundException("Report not found or you have no access to report!");
        }

        scanAssertService.assertUserHasAccessToReport(report);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(report);
        scanReportSensitiveDataObfuscator.obfuscate(scanSecHubReport);

        return scanSecHubReport;
    }

}
