package com.daimler.sechub.domain.scan.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.web.SecHubReportWeb;
import com.daimler.sechub.commons.model.web.SecHubReportWebAttack;
import com.daimler.sechub.commons.model.web.SecHubReportWebRequest;
import com.daimler.sechub.commons.model.web.SecHubReportWebResponse;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

@Component
public class FalsePositiveMetaDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(FalsePositiveMetaDataFactory.class);

    /**
     * Creates meta data for given finding
     *
     * @param finding
     * @return meta data, never <code>null</code>
     */
    public FalsePositiveMetaData createMetaData(SecHubFinding finding) {
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
        case WEB_SCAN:
            return createWebScan(finding);
        default:
            throw new NotAcceptableException("A false positive handling for type " + type + " is currently not implemented!");
        }
    }

    private FalsePositiveMetaData createWebScan(SecHubFinding finding) {
        FalsePositiveMetaData metaData = createCommonMetaDataWithCweIdEnsured(finding);
        metaData.setCveId(finding.getCveId());
        metaData.setScanType(ScanType.WEB_SCAN);

        FalsePositiveWebMetaData web = new FalsePositiveWebMetaData();
        SecHubReportWeb findingWeb = finding.getWeb();
        if (findingWeb == null) {
            throw new IllegalStateException("False positive handling for web scan not possible - finding does not contain web data?!?");
        }
        SecHubReportWebAttack findingAttack = findingWeb.getAttack();
        SecHubReportWebRequest findingRequest = findingWeb.getRequest();
        SecHubReportWebResponse findingResponse = findingWeb.getResponse();

        FalsePositiveWebRequestMetaData falsePositiveRequestMetaData = web.getRequest();
        falsePositiveRequestMetaData.setAttackVector(findingAttack.getVector());
        falsePositiveRequestMetaData.setMethod(findingRequest.getMethod());
        falsePositiveRequestMetaData.setTarget(findingRequest.getTarget());
        falsePositiveRequestMetaData.setProtocol(findingRequest.getProtocol());
        falsePositiveRequestMetaData.setVersion(findingRequest.getVersion());

        FalsePositiveWebResponseMetaData falsePositiveResponseMetaData = web.getResponse();
        falsePositiveResponseMetaData.setEvidence(findingAttack.getEvidence().getSnippet());
        falsePositiveResponseMetaData.setStatusCode(findingResponse.getStatusCode());

        metaData.setWeb(web);

        return metaData;
    }

    private FalsePositiveMetaData createCodeScan(SecHubFinding finding) {
        FalsePositiveMetaData metaData = createCommonMetaDataWithCweIdEnsured(finding);

        metaData.setScanType(ScanType.CODE_SCAN);

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

    private FalsePositiveMetaData createCommonMetaDataWithCweIdEnsured(SecHubFinding finding) {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setName(finding.getName());
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
        return metaData;
    }

    private FalsePositiveCodePartMetaData importCallStackElement(SecHubCodeCallStack callstack) {
        if (callstack == null) {
            return null;
        }
        FalsePositiveCodePartMetaData start = new FalsePositiveCodePartMetaData();
        start.setLocation(callstack.getLocation());
        start.setRelevantPart(callstack.getRelevantPart());
        start.setSourceCode(callstack.getSource());
        return start;
    }
}
