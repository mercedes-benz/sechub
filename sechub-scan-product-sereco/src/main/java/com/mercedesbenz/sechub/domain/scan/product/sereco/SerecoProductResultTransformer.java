// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportVersion;
import com.mercedesbenz.sechub.commons.model.SecHubRevisionData;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.SecHubVersionControlData;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWeb;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebAttack;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebBodyLocation;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebEvidence;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebRequest;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebResponse;
import com.mercedesbenz.sechub.domain.scan.ReportTransformationResult;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.report.ReportProductResultTransformer;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoClassification;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoRevisionData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVersionControl;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebAttack;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebBodyLocation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebEvidence;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebRequest;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebResponse;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

@Component
public class SerecoProductResultTransformer implements ReportProductResultTransformer {

    @Autowired
    SerecoFalsePositiveMarker falsePositiveMarker;

    @Value("${sechub.feature.showProductResultLink:false}")
    @MustBeDocumented(scope = DocumentationScopeConstants.SCOPE_ADMINISTRATION, value = "Administrators can turn on this mode to allow product links in json and HTML output")
    boolean showProductLineResultLink;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoProductResultTransformer.class);

    @Override
    public ReportTransformationResult transform(ProductResult serecoProductResult) throws SecHubExecutionException {
        String origin = serecoProductResult.getResult();
        String projectId = serecoProductResult.getProjectId();
        UUID sechubJobUUID = serecoProductResult.getSecHubJobUUID();

        SerecoMetaData data = null;
        if (origin != null && !origin.isEmpty()) {
            data = JSONConverter.get().fromJSON(SerecoMetaData.class, origin);
        } else {
            data = new SerecoMetaData(); // fallback
            LOG.warn("No sereco data was available! Did fallback to empty meta data");
        }

        falsePositiveMarker.markFalsePositives(projectId, data.getVulnerabilities());

        ReportTransformationResult transformerResult = new ReportTransformationResult();
        transformerResult.setReportVersion(SecHubReportVersion.VERSION_1_0.getVersionAsString());
        transformerResult.setJobUUID(sechubJobUUID);

        List<SecHubFinding> findings = transformerResult.getResult().getFindings();

        int findingId = 0;
        for (SerecoVulnerability vulnerability : data.getVulnerabilities()) {
            findingId++;
            if (vulnerability.isFalsePositive()) {
                /*
                 * we do not add false positives to report - so we store only real positives.
                 * False positive data is still available in SeReCo results and so in admin scan
                 * logs,
                 */
                continue;
            }
            SecHubFinding finding = new SecHubFinding();
            handleClassifications(finding, vulnerability, serecoProductResult.getSecHubJobUUID());

            finding.setDescription(vulnerability.getDescription());
            finding.setName(vulnerability.getType());
            finding.setSolution(vulnerability.getSolution());
            finding.setId(findingId);
            finding.setSeverity(transformSeverity(vulnerability.getSeverity()));

            if (showProductLineResultLink) {
                finding.setProductResultLink(vulnerability.getProductResultLink());
            }
            ScanType scanType = vulnerability.getScanType();
            finding.setType(scanType);

            if (scanType == null) {
                // this should normally only happen for artificial vulnerability which
                // were added for SecHub failures (a legacy feature which will be removed in
                // future).
                scanType = ScanType.UNKNOWN;
                LOG.debug("Finding:{} '{}' has no scan type set. Use {} as fallback.", findingId, vulnerability.getType(), scanType);
            }

            handleRevisionData(vulnerability, finding);

            switch (scanType) {
            case CODE_SCAN:
            case SECRET_SCAN:
            case IAC_SCAN:
                finding.setCode(convert(vulnerability.getCode()));
                break;
            case INFRA_SCAN:
                break;
            case WEB_SCAN:
                appendWebData(sechubJobUUID, vulnerability, finding);
                break;
            default:
                break;

            }

            findings.add(finding);
        }

        // we sort the findings
        Collections.sort(findings);

        handleAnnotations(sechubJobUUID, data, transformerResult);

        handleMetaData(data, transformerResult);

        /* when status is not set already, no failure has appeared and we mark as OK */
        if (transformerResult.getStatus() == null) {
            transformerResult.setStatus(SecHubStatus.SUCCESS);
        }

        return transformerResult;
    }

    private void handleMetaData(SerecoMetaData data, ReportTransformationResult transformerResult) {
        /* ensure metadata never null */
        if (transformerResult.getMetaData() == null) {
            transformerResult.setMetaData(new SecHubReportMetaData());
        }

        SecHubReportMetaData metaData = transformerResult.getMetaData();
        SerecoVersionControl serecoVersionControl = data.getVersionControl();
        handleVersionControlMetaData(metaData, serecoVersionControl);

    }

    private void handleVersionControlMetaData(SecHubReportMetaData metaData, SerecoVersionControl serecoVersionControl) {
        if (serecoVersionControl == null) {
            return;
        }
        SecHubVersionControlData versionControlData = new SecHubVersionControlData();
        metaData.setVersionControl(versionControlData);

        versionControlData.setLocation(serecoVersionControl.getLocation());

        String serecoRevisionId = serecoVersionControl.getRevisionId();
        if (serecoRevisionId != null) {
            SecHubRevisionData revisionData = new SecHubRevisionData();
            revisionData.setId(serecoRevisionId);

            versionControlData.setRevision(revisionData);
        }
    }

    private void handleRevisionData(SerecoVulnerability vulnerability, SecHubFinding finding) {
        SerecoRevisionData serecoRevision = vulnerability.getRevision();
        if (serecoRevision != null) {
            SecHubRevisionData revisionData = new SecHubRevisionData();
            revisionData.setId(serecoRevision.getId());
            finding.setRevision(revisionData);

        }
    }

    private void appendWebData(UUID sechubJobUUID, SerecoVulnerability vulnerability, SecHubFinding finding) {
        SecHubReportWeb sechubWeb = new SecHubReportWeb();
        SecHubReportWebRequest sechubRequest = sechubWeb.getRequest();
        SerecoWeb serecoWeb = vulnerability.getWeb();
        if (serecoWeb == null) {
            LOG.error("Web scan, but vulnerability has no web object inside - must skip finding {} for report with uuid=", finding.getId(), sechubJobUUID);
            return;
        }
        /* request */
        SerecoWebRequest serecoRequest = serecoWeb.getRequest();
        sechubRequest.setProtocol(serecoRequest.getProtocol());
        sechubRequest.setVersion(serecoRequest.getVersion());
        sechubRequest.setTarget(serecoRequest.getTarget());
        sechubRequest.setMethod(serecoRequest.getMethod());

        sechubRequest.getHeaders().putAll(serecoRequest.getHeaders());

        sechubRequest.getBody().setText(serecoRequest.getBody().getText());
        sechubRequest.getBody().setBinary(serecoRequest.getBody().getBinary());

        /* response */
        SerecoWebResponse serecoResponse = serecoWeb.getResponse();
        SecHubReportWebResponse sechubResponse = sechubWeb.getResponse();
        sechubResponse.setStatusCode(serecoResponse.getStatusCode());
        sechubResponse.setReasonPhrase(serecoResponse.getReasonPhrase());
        sechubResponse.setProtocol(serecoResponse.getProtocol());
        sechubResponse.setVersion(serecoResponse.getVersion());
        sechubResponse.getHeaders().putAll(serecoResponse.getHeaders());

        sechubResponse.getBody().setText(serecoResponse.getBody().getText());
        sechubResponse.getBody().setBinary(serecoResponse.getBody().getBinary());

        /* attack */
        SerecoWebAttack serecoAttack = serecoWeb.getAttack();
        SecHubReportWebAttack sechubAttack = sechubWeb.getAttack();
        sechubAttack.setVector(serecoAttack.getVector());

        SerecoWebEvidence serecoEvidence = serecoAttack.getEvidence();
        if (serecoEvidence != null) {
            SecHubReportWebEvidence sechubEvidence = new SecHubReportWebEvidence();
            sechubEvidence.setSnippet(serecoEvidence.getSnippet());

            SerecoWebBodyLocation serecoBodyLocation = serecoEvidence.getBodyLocation();
            if (serecoBodyLocation != null) {
                SecHubReportWebBodyLocation sechubBodyLocation = new SecHubReportWebBodyLocation();
                sechubBodyLocation.setStartLine((serecoBodyLocation.getStartLine()));
                sechubEvidence.setBodyLocation(sechubBodyLocation);
            }
            sechubAttack.setEvidence(sechubEvidence);
        }

        finding.setWeb(sechubWeb);
    }

    private void handleAnnotations(UUID sechubJobUUID, SerecoMetaData data, ReportTransformationResult transformerResult) {
        Set<SerecoAnnotation> annotations = data.getAnnotations();
        for (SerecoAnnotation annotation : annotations) {
            handleAnnotation(annotation, transformerResult, sechubJobUUID);
        }
    }

    private void handleAnnotation(SerecoAnnotation annotation, ReportTransformationResult transformerResult, UUID sechubJobUUID) {
        if (annotation == null) {
            return;
        }
        SerecoAnnotationType annotationType = annotation.getType();

        String annotationValue = annotation.getValue();
        if (annotationType == null) {
            LOG.error("Sereco message type not set for message :{}, sechub job uuid: {}", annotationValue, sechubJobUUID);
            return;
        }

        switch (annotationType) {
        case USER_INFO:
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.INFO, annotationValue));
            break;
        case INTERNAL_PRODUCT_CANCELED:
            transformerResult.setAtLeastOneProductCanceled(true);
            break;
        case USER_WARNING:
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.WARNING, annotationValue));
            break;
        case USER_ERROR:
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.ERROR, annotationValue));
            break;
        case INTERNAL_ERROR_PRODUCT_FAILED:
            /* internal errors are marked with status failed */
            transformerResult.setStatus(SecHubStatus.FAILED);
            /* we add an information to user as well */
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.ERROR, "Job execution failed because of an internal problem!"));
            break;
        case INTERNAL_INFO_PRODUCT_SUCCESSFUL_IMPORTED:
            /*
             * at least one product result was successful imported - means no graceful fall
             * through, but real product data available in at least one product.
             */
            transformerResult.setAtLeastOneRealProductResultContained(true);
            break;
        default:
            // nothing
            LOG.error("Unhandled sereco annotation type:{}, value:{}, sechub job uuid: {}", annotationType, annotationValue, sechubJobUUID);
        }

    }

    private void appendSecHubMessage(ReportTransformationResult transformerResult, SecHubMessage sechubMessage) {
        if (sechubMessage != null) {
            transformerResult.getMessages().add(sechubMessage);
        }
    }

    private void handleClassifications(SecHubFinding finding, SerecoVulnerability v, UUID jobUUID) {
        SerecoClassification clazz = v.getClassification();
        String cwe = clazz.getCwe();
        if (cwe != null) {
            try {
                int cweInt = Integer.parseInt(cwe);
                finding.setCweId(cweInt);
            } catch (NumberFormatException e) {
                LOG.error("CWE information not valid:{} inside result for job:{}", cwe, jobUUID);
            }
        }
        finding.setCveId(clazz.getCve());
    }

    private SecHubCodeCallStack convert(SerecoCodeCallStackElement element) {
        if (element == null) {
            return null;
        }

        SecHubCodeCallStack codeCallStack = new SecHubCodeCallStack();
        codeCallStack.setLine(element.getLine());
        codeCallStack.setColumn(element.getColumn());
        codeCallStack.setLocation(element.getLocation());
        codeCallStack.setSource(element.getSource());
        codeCallStack.setCalls(convert(element.getCalls()));
        codeCallStack.setRelevantPart(element.getRelevantPart());

        return codeCallStack;
    }

    private Severity transformSeverity(com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity metaSeverity) {
        if (metaSeverity == null) {
            LOG.error("Missing Sereco Severity cannot transformed {} to sechub result! So returning unclassified!", metaSeverity);
            return Severity.UNCLASSIFIED;
        }
        for (Severity severity : Severity.values()) {
            if (severity.name().equals(metaSeverity.name())) {
                return severity;
            }
        }
        LOG.error("Was not able to tranform Sereco Severity:{} to sechub result! So returning unclassified!", metaSeverity);
        return Severity.UNCLASSIFIED;
    }

    @Override
    public boolean canTransform(ProductIdentifier productIdentifier) {
        return ProductIdentifier.SERECO.equals(productIdentifier);
    }

}
