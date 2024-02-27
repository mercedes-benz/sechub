// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class FalsePositiveJobDataService {

    private static final ScanProjectConfigID CONFIG_ID = ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION;

    private static final Logger LOG = LoggerFactory.getLogger(FalsePositiveJobDataService.class);

    @Autowired
    ScanReportRepository scanReportRepository;

    @Autowired
    UserInputAssertion userInputAssertion;

    @Autowired
    ScanProjectConfigService configService;

    @Autowired
    FalsePositiveJobDataListValidation falsePositiveJobDataListValidation;

    @Autowired
    FalsePositiveJobDataConfigMerger merger;

    @Autowired
    UserContextService userContextService;

    @Autowired
    ScanAssertService scanAssertService;

    @Autowired
    AuditLogService auditLogService;

    public void addFalsePositives(String projectId, FalsePositiveJobDataList data) {
        validateUserInputAndProjectAccess(projectId, data);

        auditLogService.log("triggers add of {} false postive entries to project {}", data.getJobData().size(), projectId);

        if (data.getJobData().isEmpty()) {
            LOG.debug("User false positive job data list has no entries - so skip further steps");
            return;
        }

        FalsePositiveProjectConfiguration config = fetchOrCreateConfiguration(projectId);

        addJobDataListToConfiguration(config, data);

        /* update configuration */
        configService.set(projectId, CONFIG_ID, config.toJSON());

    }

    public void removeFalsePositive(String projectId, UUID jobUUID, int findingId) {
        validateProjectIdAndProjectAccess(projectId);

        auditLogService.log("triggers remove of false positive entry from project {}: jobUUID={}, findingId={}", projectId, jobUUID, findingId);

        FalsePositiveProjectConfiguration config = fetchOrCreateConfiguration(projectId);
        FalsePositiveJobData jobDataToRemove = new FalsePositiveJobData();
        jobDataToRemove.setJobUUID(jobUUID);
        jobDataToRemove.setFindingId(findingId);

        merger.removeJobDataWithMetaDataFromConfig(config, jobDataToRemove);

        /* update configuration */
        configService.set(projectId, CONFIG_ID, config.toJSON());

    }

    public FalsePositiveProjectConfiguration fetchFalsePositivesProjectConfiguration(String projectId) {
        validateProjectIdAndProjectAccess(projectId);

        FalsePositiveProjectConfiguration config = fetchOrCreateConfiguration(projectId);

        return config;
    }

    private void validateUserInputAndProjectAccess(String projectId, FalsePositiveJobDataList data) {
        validateProjectIdAndProjectAccess(projectId);
        assertValid(data, falsePositiveJobDataListValidation);
    }

    private void validateProjectIdAndProjectAccess(String projectId) {
        userInputAssertion.assertIsValidProjectId(projectId);
        scanAssertService.assertUserHasAccessToProject(projectId);
    }

    private void addJobDataListToConfiguration(FalsePositiveProjectConfiguration config, FalsePositiveJobDataList jobDataList) {
        List<FalsePositiveJobData> list = jobDataList.getJobData();

        /*
         * Reason for sorting: we want to load reports only one time, so sort by report
         * job UUID is necessary for method "fetchReportIfNotAlreadyLoaded"
         */
        list.sort(Comparator.comparing(FalsePositiveJobData::getJobUUID));

        ScanSecHubReport currentReport = null;

        for (FalsePositiveJobData data : list) {
            if (merger.isFalsePositiveEntryAlreadyExisting(config, data)) {
                LOG.debug("Skip processing because FP already defined: {}", data);
                continue;
            }

            UUID jobUUID = data.getJobUUID();
            currentReport = fetchReportIfNotAlreadyLoaded(jobUUID, currentReport);

            merger.addJobDataWithMetaDataToConfig(currentReport, config, data, userContextService.getUserId());
        }

    }

    private ScanSecHubReport fetchReportIfNotAlreadyLoaded(UUID jobUUID, ScanSecHubReport currentReport) {

        /* load report if it is not the current report */
        if (currentReport == null || !jobUUID.equals(currentReport.getJobUUID())) {
            ScanReport scanReport = scanReportRepository.findBySecHubJobUUID(jobUUID);
            if (scanReport == null) {
                throw new NotFoundException("No report found for job " + jobUUID);
            }
            currentReport = new ScanSecHubReport(scanReport);
        }
        return currentReport;
    }

    private FalsePositiveProjectConfiguration fetchOrCreateConfiguration(String projectId) {
        ScanProjectConfig projectConfig = configService.getOrCreate(projectId, CONFIG_ID, false, "{}"); // access check unnecessary, already done

        FalsePositiveProjectConfiguration falsePositiveConfiguration = FalsePositiveProjectConfiguration.fromJSONString(projectConfig.getData());
        return falsePositiveConfiguration;
    }

}
