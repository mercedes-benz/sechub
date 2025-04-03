// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWeb;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebAttack;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebRequest;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebResponse;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;

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
            /* Maybe an old report where type was not set */
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
        case SECRET_SCAN:
            return createSecretScan(finding);
        case IAC_SCAN:
            return createIacScan(finding);
        case WEB_SCAN:
            return createWebScan(finding);
        default:
            throw new NotAcceptableException("A false positive handling for type " + type + " is currently not implemented!");
        }
    }

    private FalsePositiveMetaData createWebScan(SecHubFinding finding) {
        FalsePositiveMetaData metaData = createCommonMetaDataWithCweId(finding);
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

    private FalsePositiveMetaData createSecretScan(SecHubFinding finding) {
        return createCodeBasedMetaData(finding, ScanType.SECRET_SCAN);
    }

    private FalsePositiveMetaData createIacScan(SecHubFinding finding) {
        return createCodeBasedMetaData(finding, ScanType.IAC_SCAN);
    }

    private FalsePositiveMetaData createCodeScan(SecHubFinding finding) {
        return createCodeBasedMetaData(finding, ScanType.CODE_SCAN);
    }

    private FalsePositiveMetaData createCodeBasedMetaData(SecHubFinding finding, ScanType scanType) {
        FalsePositiveMetaData metaData = createCommonMetaDataWithCweId(finding);
        metaData.setScanType(scanType);
        appendCommonCodeBasedParts(finding, metaData);
        return metaData;
    }

    private void appendCommonCodeBasedParts(SecHubFinding finding, FalsePositiveMetaData metaData) {
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
    }

    private FalsePositiveMetaData createCommonMetaDataWithCweId(SecHubFinding finding) {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setName(finding.getName());
        metaData.setSeverity(finding.getSeverity());

        Integer cweId = finding.getCweId();
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
