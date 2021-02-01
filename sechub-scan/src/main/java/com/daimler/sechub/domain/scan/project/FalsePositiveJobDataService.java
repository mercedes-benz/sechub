// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static com.daimler.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.ScanAssertService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportRepository;
import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class FalsePositiveJobDataService {

    private static final ScanProjectConfigID CONFIG_ID = ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION;

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

        dropMetaData(config);
        return config;
    }

    private void dropMetaData(FalsePositiveProjectConfiguration config) {
    }

    private void validateUserInputAndProjectAccess(String projectId, FalsePositiveJobDataList data) {
        validateProjectIdAndProjectAccess(projectId);
        assertValid(data, falsePositiveJobDataListValidation);
    }

    private void validateProjectIdAndProjectAccess(String projectId) {
        userInputAssertion.isValidProjectId(projectId);
        scanAssertService.assertUserHasAccessToProject(projectId);
    }

    private void addJobDataListToConfiguration(FalsePositiveProjectConfiguration config, FalsePositiveJobDataList jobDataList) {
        List<FalsePositiveJobData> list = jobDataList.getJobData();

        /* we want to load reports only one time, so sort by report job UUID... */
        list.sort(Comparator.comparing(FalsePositiveJobData::getJobUUID));

        ScanReportResult scanReportResult = null;
        for (FalsePositiveJobData data : list) {
            UUID jobUUID = data.getJobUUID();

            if (scanReportResult == null || !jobUUID.equals(scanReportResult.getJobUUID())) {
                ScanReport report = scanReportRepository.findBySecHubJobUUID(jobUUID);
                if (report == null) {
                    throw new NotFoundException("No report found for job " + jobUUID);
                }
                scanReportResult = new ScanReportResult(report);
            }
            merger.addJobDataWithMetaDataToConfig(scanReportResult, config, data, userContextService.getUserId());
        }

    }

    private FalsePositiveProjectConfiguration fetchOrCreateConfiguration(String projectId) {
        ScanProjectConfig projectConfig = configService.getOrCreate(projectId, CONFIG_ID, false, "{}"); // access check unnecessary, already done

        FalsePositiveProjectConfiguration falsePositiveConfiguration = FalsePositiveProjectConfiguration.fromJSONString(projectConfig.getData());
        return falsePositiveConfiguration;
    }

}
