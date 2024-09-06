// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

/**
 * Merges job based false positive data, meta data from origin report into
 * project false positive configuration. Does also validate that meta data is
 * available - e.g. CWE identifier must be available for code scans.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class FalsePositiveDataConfigMerger {

    private static final Logger LOG = LoggerFactory.getLogger(FalsePositiveDataConfigMerger.class);

    @Autowired
    FalsePositiveMetaDataFactory metaDataFactory;

    public void addJobDataWithMetaDataToConfig(ScanSecHubReport report, FalsePositiveProjectConfiguration config, FalsePositiveJobData falsePositiveJobData,
            String author) {

        SecHubFinding finding = fetchFindingInReportOrFail(report, falsePositiveJobData);

        FalsePositiveEntry existingEntry = findExistingFalsePositiveEntryInConfig(config, falsePositiveJobData);
        if (existingEntry != null) {
            LOG.warn("False positive entry for job:{}, findingId:{} not added, because already existing", falsePositiveJobData.getJobUUID(),
                    falsePositiveJobData.getFindingId());
            return;
        }

        FalsePositiveMetaData metaData = metaDataFactory.createMetaData(finding);

        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setAuthor(author);
        entry.setJobData(falsePositiveJobData);
        entry.setMetaData(metaData);

        config.getFalsePositives().add(entry);

    }

    public void addFalsePositiveProjectDataEntryOrUpdateExisting(FalsePositiveProjectConfiguration config, FalsePositiveProjectData projectData,
            String userId) {
        FalsePositiveEntry projectDataEntry = new FalsePositiveEntry();
        projectDataEntry.setAuthor(userId);
        projectDataEntry.setProjectData(projectData);

        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        for (int index = 0; index < falsePositives.size(); index++) {
            FalsePositiveEntry existingFPEntry = falsePositives.get(index);
            FalsePositiveProjectData projectDataFromEntry = existingFPEntry.getProjectData();
            if (projectDataFromEntry == null) {
                LOG.debug("The entry is a jobData entry with metaData so no projectData");
                continue;
            }
            if (projectDataFromEntry.getId().equals(projectData.getId())) {
                LOG.info("False positive project data entry with id: '{}', will be updated with new data!", projectData.getId());
                falsePositives.set(index, projectDataEntry);
                return;
            }
        }
        falsePositives.add(projectDataEntry);
    }

    public void removeJobDataWithMetaDataFromConfig(FalsePositiveProjectConfiguration config, FalsePositiveJobData jobDataToRemove) {
        FalsePositiveEntry entry = findExistingFalsePositiveEntryInConfig(config, jobDataToRemove);
        if (entry != null) {
            config.getFalsePositives().remove(entry);
        }

    }

    public void removeProjectDataFromConfig(FalsePositiveProjectConfiguration config, FalsePositiveProjectData projectDataToRemove) {
        FalsePositiveEntry entry = findExistingProjectDataFalsePositiveEntryInConfig(config, projectDataToRemove);
        if (entry != null) {
            config.getFalsePositives().remove(entry);
        }

    }

    public boolean isFalsePositiveEntryAlreadyExisting(FalsePositiveProjectConfiguration config, FalsePositiveJobData falsePositiveJobData) {
        return findExistingFalsePositiveEntryInConfig(config, falsePositiveJobData) != null;
    }

    private FalsePositiveEntry findExistingProjectDataFalsePositiveEntryInConfig(FalsePositiveProjectConfiguration config,
            FalsePositiveProjectData projectDataToRemove) {
        for (FalsePositiveEntry existingFPEntry : config.getFalsePositives()) {
            FalsePositiveProjectData projectDataFromEntry = existingFPEntry.getProjectData();
            if (projectDataFromEntry == null) {
                LOG.debug("The entry is a jobData entry with metaData so no projectData");
                continue;
            }

            if (projectDataFromEntry.getId().equals(projectDataToRemove.getId())) {
                return existingFPEntry;
            }
        }
        return null;
    }

    private FalsePositiveEntry findExistingFalsePositiveEntryInConfig(FalsePositiveProjectConfiguration config, FalsePositiveJobData falsePositiveJobData) {
        for (FalsePositiveEntry existingFPEntry : config.getFalsePositives()) {
            FalsePositiveJobData jobData = existingFPEntry.getJobData();
            if (jobData == null) {
                LOG.debug("The entry is a projectData entry so no jobData and metaData");
                continue;
            }

            if (!jobData.getJobUUID().equals(falsePositiveJobData.getJobUUID())) {
                continue;
            }
            if (jobData.getFindingId() != falsePositiveJobData.getFindingId()) {
                continue;
            }
            return existingFPEntry;
        }
        return null;
    }

    private SecHubFinding fetchFindingInReportOrFail(ScanSecHubReport report, FalsePositiveJobData falsePositiveJobData) {
        SecHubResult result = report.getResult();

        for (SecHubFinding finding : result.getFindings()) {
            if (finding.getId() == falsePositiveJobData.getFindingId()) {
                return finding;
            }
        }
        throw new NotFoundException("No finding with id:" + falsePositiveJobData.getFindingId() + " found inside report for job:" + report.getJobUUID());
    }

}
