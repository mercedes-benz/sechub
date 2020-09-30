// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.error.NotFoundException;

/**
 * Merges job based false positive data, meta data from origin report into
 * project false positive configuration. Does also validate that meta data is
 * available - e.g. CWE identifier must be available for code scans.
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class FalsePositiveJobDataConfigMerger {

    private static final Logger LOG = LoggerFactory.getLogger(FalsePositiveJobDataConfigMerger.class);

    public void addJobDataWithMetaDataToConfig(ScanReportResult scanReportResult, FalsePositiveProjectConfiguration config,
            FalsePositiveJobData falsePositiveJobData, String author) {

        SecHubFinding finding = fetchFindingInReportOrFail(scanReportResult, falsePositiveJobData);

        FalsePositiveEntry existingEntry = findExistingFalsePositiveEntryInConfig(config, falsePositiveJobData);
        if (existingEntry != null) {
            LOG.warn("False positive entry for job:{}, findingId:{} not added, because already existing", falsePositiveJobData.getJobUUID(),
                    falsePositiveJobData.getFindingId());
            return;
        }

        FalsePositiveMetaData metaData = createMetaData(finding);

        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setAuthor(author);
        entry.setJobData(falsePositiveJobData);
        entry.setMetaData(metaData);

        config.getFalsePositives().add(entry);

    }

    public void removeJobDataWithMetaDataFromConfig(FalsePositiveProjectConfiguration config, FalsePositiveJobData jobDataToRemove) {
        FalsePositiveEntry entry = findExistingFalsePositiveEntryInConfig(config, jobDataToRemove);
        if (entry == null) {
            return;
        }
        config.getFalsePositives().remove(entry);
    }

    private FalsePositiveMetaData createMetaData(SecHubFinding finding) {
        ScanType type = finding.getType();
        if (type == null) {
            /* hmm.. maybe an old report where type was not set */
            SecHubCodeCallStack callstack = finding.getCode();
            if (callstack == null) {
                throw new IllegalStateException(
                        "Sorry, cannot determine scan type which is necessary for false positive handling. Please start a new scanjob and use this job UUID and retry.");
            }
            type = ScanType.CODE_SCAN;
            LOG.warn("scan type was not given - fallback to {}", type);
        }

        switch (type) {
        case CODE_SCAN:
            return createCodeScan(finding);
        default:
            throw new NotAcceptableException("A false positive handling for given type:{} is currently not suported!");
        }
    }

    private FalsePositiveMetaData createCodeScan(SecHubFinding finding) {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setName(finding.getName());
        metaData.setScanType(ScanType.CODE_SCAN);
        metaData.setSeverity(finding.getSeverity());

        /* CWE id is used to identify same code weaknes accross products */
        Integer cweId = finding.getCweId();
        if (cweId == null) {
            /*
             * old sechub results do not contain CWE information - so a new scan is
             * necessary to create cwe identifier inside next report
             */
            throw new NotAcceptableException("No CWE identifier found in given sechub finding " + finding.getId() + ":" + finding.getName()
                    + ", so cannot mark false positives!\n"
                    + "This could be a migration issue from an older report which did not cotain such information. Please just execute a new scan job and retry to mark false positives by new finding");
        }

        metaData.setCweId(cweId);

        FalsePositiveCodeMetaData code = new FalsePositiveCodeMetaData();

        SecHubCodeCallStack startCallStack = finding.getCode();
        if (startCallStack == null) {
            throw new IllegalStateException("Callstack must be given to create code scan meta data");
        }
        SecHubCodeCallStack endCallStack = startCallStack.getCalls();
        while (endCallStack != null && endCallStack.getCalls() != null) {
            endCallStack = endCallStack.getCalls();
        }

        code.setStart(importCallStackElement(startCallStack));
        code.setEnd(importCallStackElement(endCallStack));
        metaData.setCode(code);

        return metaData;
    }

    private FalsePositiveCodePartMetaData importCallStackElement(SecHubCodeCallStack callstack) {
        if (callstack==null) {
            return null;
        }
        FalsePositiveCodePartMetaData start = new FalsePositiveCodePartMetaData();
        start.setLocation(callstack.getLocation());
        start.setRelevantPart(callstack.getRelevantPart());
        start.setSourceCode(callstack.getSource());
        return start;
    }

    private FalsePositiveEntry findExistingFalsePositiveEntryInConfig(FalsePositiveProjectConfiguration config, FalsePositiveJobData falsePositiveJobData) {
        for (FalsePositiveEntry existingFPEntry : config.getFalsePositives()) {
            FalsePositiveJobData jobData = existingFPEntry.getJobData();
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

    private SecHubFinding fetchFindingInReportOrFail(ScanReportResult scanReportResult, FalsePositiveJobData falsePositiveJobData) {
        SecHubResult result = scanReportResult.getResult();

        for (SecHubFinding finding : result.getFindings()) {
            if (finding.getId() == falsePositiveJobData.getFindingId()) {
                return finding;
            }
        }
        throw new NotFoundException(
                "No finding with id:" + falsePositiveJobData.getFindingId() + " found inside report for job:" + scanReportResult.getJobUUID());
    }

}
