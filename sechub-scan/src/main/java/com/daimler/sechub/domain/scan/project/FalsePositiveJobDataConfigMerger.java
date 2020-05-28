package com.daimler.sechub.domain.scan.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.SecHubCodeCallStack;
import com.daimler.sechub.domain.scan.SecHubFinding;
import com.daimler.sechub.domain.scan.SecHubResult;
import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.type.ScanType;

/**
 * Merges job based false positive data, meta data from origin report into project false positive configuration 
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
            throw new NotAcceptableException("A false positive hanlding for given type:{} is currently not suported!")  ;
        }
    }

    private FalsePositiveMetaData createCodeScan(SecHubFinding finding) {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setName(finding.getName());
        metaData.setScanType(ScanType.CODE_SCAN);
        metaData.setSeverity(finding.getSeverity());
        FalsePositiveCodeMetaData code = new FalsePositiveCodeMetaData();
        
        SecHubCodeCallStack startCallStack = finding.getCode();
        if (startCallStack==null) {
            throw new IllegalStateException("Callstack must be given to create code scan meta data");
        }
        SecHubCodeCallStack endCallStack = startCallStack.getCalls(); 
        while (endCallStack!=null && endCallStack.getCalls()!=null) {
            endCallStack = endCallStack.getCalls(); 
        }
        
        code.setStart(importCallStackElement(startCallStack));
        code.setEnd(importCallStackElement(endCallStack));
        metaData.setCode(code);
        
        return metaData;
    }

    private FalsePositiveCodePartMetaData importCallStackElement(SecHubCodeCallStack startCallStack) {
        FalsePositiveCodePartMetaData start = new FalsePositiveCodePartMetaData();
        start.setLocation(startCallStack.getLocation());
        start.setRelevantPart(startCallStack.getRelevantPart());
        start.setSourceCode(startCallStack.getSource());
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
