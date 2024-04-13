// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.autocleanup;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.config.ScanConfigService;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseScanAutoCleanExecution;

@Service
public class ScanAutoCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanAutoCleanupService.class);

    @Autowired
    TimeCalculationService timeCalculationService;

    @Autowired
    ScanConfigService configService;

    @Autowired
    ProjectScanLogRepository projectScanLogRepository;

    @Autowired
    ScanReportRepository scanReportRepository;

    @Autowired
    ProductResultRepository productResultRepository;

    @Autowired
    AutoCleanupResultInspector inspector;

    @UseCaseScanAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();
        if (days == 0) {
            LOG.trace("Cancel schedule auto cleanup because disabled.");
            return;
        }
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        deleteProductResults(days, cleanTimeStamp);
        deleteScanResults(days, cleanTimeStamp);
        deleteScanLogs(days, cleanTimeStamp);

    }

    private void deleteScanLogs(long days, LocalDateTime cleanTimeStamp) {
        int amount = projectScanLogRepository.deleteLogsOlderThan(cleanTimeStamp);
        /* @formatter:off */
        inspector.inspect(AutoCleanupResult.builder().
                autoCleanup("scan-logs",getClass()).
                forDays(days).
                hasDeleted(amount).
                byTimeStamp(cleanTimeStamp).
                build()
                );
        /* @formatter:on */
    }

    private void deleteScanResults(long days, LocalDateTime cleanTimeStamp) {
        /* @formatter:off */
        int amount = scanReportRepository.deleteReportsOlderThan(cleanTimeStamp);
        inspector.inspect(AutoCleanupResult.builder().
                autoCleanup("scan-reports",getClass()).
                forDays(days).
                hasDeleted(amount).
                byTimeStamp(cleanTimeStamp).
                build()
                );
        /* @formatter:on */
    }

    private void deleteProductResults(long days, LocalDateTime cleanTimeStamp) {
        int amount = productResultRepository.deleteResultsOlderThan(cleanTimeStamp);
        /* @formatter:off */
        inspector.inspect(AutoCleanupResult.builder().
                autoCleanup("product-results",getClass()).
                forDays(days).
                hasDeleted(amount).
                byTimeStamp(cleanTimeStamp).
                build()
                );
        /* @formatter:on */
    }

}
