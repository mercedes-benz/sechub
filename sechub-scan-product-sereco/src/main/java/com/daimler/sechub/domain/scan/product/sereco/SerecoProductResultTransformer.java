// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubMessage;
import com.daimler.sechub.commons.model.SecHubMessageType;
import com.daimler.sechub.commons.model.SecHubReportVersion;
import com.daimler.sechub.commons.model.SecHubStatus;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.commons.model.web.SecHubReportWeb;
import com.daimler.sechub.commons.model.web.SecHubReportWebAttack;
import com.daimler.sechub.commons.model.web.SecHubReportWebBodyLocation;
import com.daimler.sechub.commons.model.web.SecHubReportWebEvidence;
import com.daimler.sechub.commons.model.web.SecHubReportWebRequest;
import com.daimler.sechub.commons.model.web.SecHubReportWebResponse;
import com.daimler.sechub.domain.scan.ReportTransformationResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.report.ReportProductResultTransformer;
import com.daimler.sechub.sereco.metadata.SerecoAnnotation;
import com.daimler.sechub.sereco.metadata.SerecoAnnotationType;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.metadata.SerecoWeb;
import com.daimler.sechub.sereco.metadata.SerecoWebAttack;
import com.daimler.sechub.sereco.metadata.SerecoWebBodyLocation;
import com.daimler.sechub.sereco.metadata.SerecoWebEvidence;
import com.daimler.sechub.sereco.metadata.SerecoWebRequest;
import com.daimler.sechub.sereco.metadata.SerecoWebResponse;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

@Component
public class SerecoProductResultTransformer implements ReportProductResultTransformer {

    @Autowired
    SerecoFalsePositiveMarker falsePositiveMarker;

    @Value("${sechub.feature.showProductResultLink:false}")
    @MustBeDocumented(scope = "administration", value = "Administrators can turn on this mode to allow product links in json and HTML output")
    boolean showProductLineResultLink;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoProductResultTransformer.class);

    @Override
    public ReportTransformationResult transform(ProductResult serecoProductResult) throws SecHubExecutionException {
        String origin = serecoProductResult.getResult();
        String projectId = serecoProductResult.getProjectId();
        UUID sechubJobUUID = serecoProductResult.getSecHubJobUUID();

        SerecoMetaData data = JSONConverter.get().fromJSON(SerecoMetaData.class, origin);

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
            finding.setId(findingId);
            finding.setSeverity(transformSeverity(vulnerability.getSeverity()));

            if (showProductLineResultLink) {
                finding.setProductResultLink(vulnerability.getProductResultLink());
            }
            ScanType scanType = vulnerability.getScanType();
            finding.setType(scanType);

            switch (scanType) {
            case CODE_SCAN:
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

        handleAnnotations(sechubJobUUID, data, transformerResult);

        /* when status is not set already, no failure has appeared and we mark as OK */
        if (transformerResult.getStatus() == null) {
            transformerResult.setStatus(SecHubStatus.SUCCESS);
        }

        return transformerResult;
    }

    private void appendWebData(UUID sechubJobUUID, SerecoVulnerability vulnerability, SecHubFinding finding) {
        SecHubReportWeb sechubWeb = new SecHubReportWeb();
        SecHubReportWebRequest sechubRequest = sechubWeb.getRequest();
        SerecoWeb serecoWeb = vulnerability.getWeb();
        if (serecoWeb==null) {
            LOG.error("Web scan, but vulnerability has no web object inside - must skip finding {} for report with uuid=",finding.getId(), sechubJobUUID);
            //break;
        }
        /* request*/
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
        
        SerecoWebEvidence serecoEvidence = serecoAttack.getEvicence();
        if (serecoEvidence!=null) {
            SecHubReportWebEvidence sechubEvidence = new SecHubReportWebEvidence();
            sechubEvidence.setSnippet(serecoEvidence.getSnippet());
            
            SerecoWebBodyLocation serecoBodyLocation = serecoEvidence.getBodyLocation();
            if (serecoBodyLocation!=null) {
                SecHubReportWebBodyLocation sechubBodyLocation = new SecHubReportWebBodyLocation();
                sechubBodyLocation.setStartLine((serecoBodyLocation.getStartLine()));
                sechubEvidence.setBodyLocation(sechubBodyLocation);
            }
            sechubAttack.setEvicence(sechubEvidence);
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
            return;
        case USER_WARNING:
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.WARNING, annotationValue));
            return;
        case USER_ERROR:
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.ERROR, annotationValue));
            return;
        case INTERNAL_ERROR_PRODUCT_FAILED:
            /* internal errors are marked with status failed */
            transformerResult.setStatus(SecHubStatus.FAILED);
            /* we add an information to user as well */
            appendSecHubMessage(transformerResult, new SecHubMessage(SecHubMessageType.ERROR, "Job execution failed because of an internal problem."));
            return;
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

    private Severity transformSeverity(com.daimler.sechub.sereco.metadata.SerecoSeverity metaSeverity) {
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
