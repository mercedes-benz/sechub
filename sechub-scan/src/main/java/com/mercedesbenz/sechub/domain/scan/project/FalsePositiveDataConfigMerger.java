// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;
import java.util.Optional;

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

        Optional<FalsePositiveEntry> optEntry = findExistingFalsePositiveEntryInConfig(config, falsePositiveJobData);

        if (optEntry.isPresent()) {
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
                // the entry is a jobData entry with metaData so no projectData
                continue;
            }
            if (projectDataFromEntry.getId().equals(projectData.getId())) {
                LOG.warn("False positive project data entry with id: '{}', will be overwriten with new data!", projectData.getId());
                falsePositives.set(index, projectDataEntry);
                return;
            }
        }
        falsePositives.add(projectDataEntry);
    }

    public void removeJobDataWithMetaDataFromConfig(FalsePositiveProjectConfiguration config, FalsePositiveJobData jobDataToRemove) {
        findExistingFalsePositiveEntryInConfig(config, jobDataToRemove).ifPresent(entry -> config.getFalsePositives().remove(entry));

    }

    public void removeProjectDataFromConfig(FalsePositiveProjectConfiguration config, FalsePositiveProjectData projectDataToRemove) {
        findExistingProjectDataFalsePositiveEntryInConfig(config, projectDataToRemove).ifPresent(entry -> config.getFalsePositives().remove(entry));

    }

    public boolean isFalsePositiveEntryAlreadyExisting(FalsePositiveProjectConfiguration config, FalsePositiveJobData falsePositiveJobData) {
        return findExistingFalsePositiveEntryInConfig(config, falsePositiveJobData) != null;
    }

    private Optional<FalsePositiveEntry> findExistingProjectDataFalsePositiveEntryInConfig(FalsePositiveProjectConfiguration config,
            FalsePositiveProjectData projectDataToRemove) {
        for (FalsePositiveEntry existingFPEntry : config.getFalsePositives()) {
            FalsePositiveProjectData projectDataFromEntry = existingFPEntry.getProjectData();
            if (projectDataFromEntry == null) {
                // the entry is a jobData entry with metaData so no projectData
                continue;
            }

            if (projectDataFromEntry.getId().equals(projectDataToRemove.getId())) {
                return Optional.of(existingFPEntry);
            }
        }
        return Optional.empty();
    }

    private Optional<FalsePositiveEntry> findExistingFalsePositiveEntryInConfig(FalsePositiveProjectConfiguration config,
            FalsePositiveJobData falsePositiveJobData) {
        for (FalsePositiveEntry existingFPEntry : config.getFalsePositives()) {
            FalsePositiveJobData jobData = existingFPEntry.getJobData();
            if (jobData == null) {
                // the entry is a projectData entry so no jobData and metaData
                continue;
            }

            if (!jobData.getJobUUID().equals(falsePositiveJobData.getJobUUID())) {
                continue;
            }
            if (jobData.getFindingId() != falsePositiveJobData.getFindingId()) {
                continue;
            }
            return Optional.of(existingFPEntry);
        }
        return Optional.empty();
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
