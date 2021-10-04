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
import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubMessage;
import com.daimler.sechub.commons.model.SecHubMessageType;
import com.daimler.sechub.commons.model.SecHubStatus;
import com.daimler.sechub.commons.model.Severity;
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
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

@Component
public class SerecoResultTransformer implements ReportProductResultTransformer {

    @Autowired
    SerecoFalsePositiveMarker falsePositiveMarker;

    @Value("${sechub.feature.showProductResultLink:false}")
    @MustBeDocumented(scope = "administration", value = "Administrators can turn on this mode to allow product links in json and HTML output")
    boolean showProductLineResultLink;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoResultTransformer.class);

    @Override
    public ReportTransformationResult transform(ProductResult serecoProductResult) throws SecHubExecutionException {
        String origin = serecoProductResult.getResult();
        String projectId = serecoProductResult.getProjectId();
        UUID sechubJobUUID = serecoProductResult.getSecHubJobUUID();

        SerecoMetaData data = JSONConverter.get().fromJSON(SerecoMetaData.class, origin);

        falsePositiveMarker.markFalsePositives(projectId, data.getVulnerabilities());

        ReportTransformationResult transformerResult = new ReportTransformationResult();

        List<SecHubFinding> findings = transformerResult.getResult().getFindings();

        int findingId = 0;
        for (SerecoVulnerability v : data.getVulnerabilities()) {
            findingId++;
            if (v.isFalsePositive()) {
                /*
                 * we do not add false positives to report - so we store only real positives.
                 * False positive data is still available in SeReCo results and so in admin scan
                 * logs,
                 */
                continue;
            }
            SecHubFinding finding = new SecHubFinding();
            handleClassifications(finding, v, serecoProductResult.getSecHubJobUUID());

            finding.setDescription(v.getDescription());
            finding.setName(v.getType());
            finding.setId(findingId);
            finding.setSeverity(transformSeverity(v.getSeverity()));

            if (showProductLineResultLink) {
                finding.setProductResultLink(v.getProductResultLink());
            }
            finding.setCode(convert(v.getCode()));
            finding.setType(v.getScanType());

            findings.add(finding);
        }

        handleAnnotations(sechubJobUUID, data, transformerResult);

        return transformerResult;
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
