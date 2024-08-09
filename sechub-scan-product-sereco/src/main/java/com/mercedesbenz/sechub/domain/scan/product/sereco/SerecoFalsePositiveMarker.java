// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.notEmpty;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Marks given vulnerabilities as false positives, if identifiable by false
 * positive configuration data for the project. will only mark and add hints
 * about reason
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoFalsePositiveMarker {

    private static final Logger LOG = LoggerFactory.getLogger(SerecoFalsePositiveMarker.class);

    @Autowired
    SerecoFalsePositiveFinder falsePositiveFinder;

    @Autowired
    SerecoProjectDataFalsePositiveFinder projectDataFalsePositiveFinder;

    @Autowired
    ScanProjectConfigService scanProjectConfigService;

    @Autowired
    SerecoProjectDataPatternMapFactory projectDataPatternMapFactory;

    public void markFalsePositives(String projectId, List<SerecoVulnerability> all) {
        notEmpty(projectId, "project id may not be null or empty!");

        if (all == null || all.isEmpty()) {
            /* no vulnerabilities found */
            return;
        }
        ScanProjectConfig config = scanProjectConfigService.get(projectId, ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION, false);
        if (config == null) {
            /* nothing configured */
            return;
        }

        String data = config.getData();
        FalsePositiveProjectConfiguration falsePositiveConfig = FalsePositiveProjectConfiguration.fromJSONString(data);
        List<FalsePositiveEntry> falsePositives = falsePositiveConfig.getFalsePositives();

        Map<String, Pattern> projectDataPatternMap = projectDataPatternMapFactory.create(falsePositives);

        for (SerecoVulnerability vulnerability : all) {

            handleVulnerability(falsePositives, vulnerability, projectDataPatternMap);
        }

    }

    private void handleVulnerability(List<FalsePositiveEntry> falsePositives, SerecoVulnerability vulnerability, Map<String, Pattern> projectDataPatternMap) {
        for (FalsePositiveEntry entry : falsePositives) {
            if (entry.getMetaData() != null) {

                if (isJobDataFalsePositive(vulnerability, entry)) {
                    vulnerability.setFalsePositive(true);
                    FalsePositiveJobData jobData = entry.getJobData();
                    vulnerability.setFalsePositiveReason("finding:" + jobData.getFindingId() + " in job:" + jobData.getJobUUID() + " marked as false positive");
                    return;
                }
            } else if (entry.getProjectData() != null) {

                if (isProjectDataFalsePositive(vulnerability, entry, projectDataPatternMap)) {
                    vulnerability.setFalsePositive(true);
                    FalsePositiveProjectData projectData = entry.getProjectData();
                    vulnerability.setFalsePositiveReason("vulnerability matches false positive project data entry with id: " + projectData.getId());
                    return;
                }
            }
        }
    }

    private boolean isProjectDataFalsePositive(SerecoVulnerability vulnerability, FalsePositiveEntry entry, Map<String, Pattern> projectDataPatternMap) {
        FalsePositiveProjectData projectData = entry.getProjectData();
        return projectDataFalsePositiveFinder.isFound(vulnerability, projectData, projectDataPatternMap);
    }

    private boolean isJobDataFalsePositive(SerecoVulnerability vulnerability, FalsePositiveEntry entry) {
        FalsePositiveMetaData metaData = entry.getMetaData();
        ScanType scanType = metaData.getScanType();
        if (scanType != vulnerability.getScanType()) {
            /* not same type - fast exit */
            return false;
        }
        if (scanType == null) {
            /* just in case ... */
            return false;
        }
        switch (scanType) {
        case CODE_SCAN:
        case SECRET_SCAN:
        case WEB_SCAN:
            return falsePositiveFinder.isFound(vulnerability, metaData);
        default:
            LOG.error("Cannot handle scan type {} - not implemented!", scanType);
            return false;
        }
    }

}
