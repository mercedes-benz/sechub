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
    ScanAssertService scanAssertService;

    public void addFalsePositives(String projectId, FalsePositiveJobDataList data) {
        /* check */
        userInputAssertion.isValidProjectId(projectId);
        scanAssertService.assertUserHasAccessToProject(projectId);
        assertValid(data, falsePositiveJobDataListValidation);

        FalsePositiveProjectConfiguration config = fetchOrCreateConfiguration(projectId);

        addJobDataListToConfiguration(config, data);

        /* update configuration */
        configService.set(projectId, CONFIG_ID, config.toJSON());

    }

    private void addJobDataListToConfiguration(FalsePositiveProjectConfiguration config, FalsePositiveJobDataList jobDataList) {
        List<FalsePositiveJobData> list = jobDataList.getJobData();

        /* qw want to load reports only one time, so sort by report job UUID... */
        list.sort(Comparator.comparing(FalsePositiveJobData::getJobUUID));

        ScanReportResult scanReportResult = null;
        ScanReport report = null;
        for (FalsePositiveJobData data : list) {
            UUID jobUUID = data.getJobUUID();

            if (report == null || !jobUUID.equals(report.getSecHubJobUUID())) {
                report = scanReportRepository.findBySecHubJobUUID(jobUUID);
                scanReportResult = new ScanReportResult(report);
            }
            addJobDataWithMetaDataToConfig(scanReportResult, config, data);

        }

    }

    private void addJobDataWithMetaDataToConfig(ScanReportResult scanReportResult, FalsePositiveProjectConfiguration config, FalsePositiveJobData data) {
        /* FIXME Albert Tregnaghi, 2020-05-26: implement this */
        throw new RuntimeException("implement me");
    }

    private FalsePositiveProjectConfiguration fetchOrCreateConfiguration(String projectId) {
        ScanProjectConfig projectConfig = configService.getOrCreate(projectId, CONFIG_ID, false, "{}"); // access check unnecessary, already done

        FalsePositiveProjectConfiguration falsePositiveConfiguration = FalsePositiveProjectConfiguration.fromJSONString(projectConfig.getData());
        return falsePositiveConfiguration;
    }

}
