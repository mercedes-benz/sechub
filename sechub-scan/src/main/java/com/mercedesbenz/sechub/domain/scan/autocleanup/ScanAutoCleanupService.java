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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdministrationAutoCleanExecution;

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

    private static boolean statistic_feature_1010_implemented = false;

    @UseCaseAdministrationAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();
        if (days == 0) {
            LOG.debug("Cancel schedule auto cleanup because disabled.");
            return;
        }
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        LOG.info("Do auto cleanup ProductResult. Everything older than {} days will be removed, means {}", days, cleanTimeStamp);
        productResultRepository.deleteResultsOlderThan(cleanTimeStamp);

        if (statistic_feature_1010_implemented) {
            LOG.info("Do auto cleanup ProductResult. Everything older than {} days will be removed, means {}", days, cleanTimeStamp);
            scanReportRepository.deleteReportsOlderThan(cleanTimeStamp);
        }
        LOG.info("Do auto cleanup ScanLog. Everything older than {} days will be removed, means {}", days, cleanTimeStamp);
        projectScanLogRepository.deleteLogsOlderThan(cleanTimeStamp);

    }

}
