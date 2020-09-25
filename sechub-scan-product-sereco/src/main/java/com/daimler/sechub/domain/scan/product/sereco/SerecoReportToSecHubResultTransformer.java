// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.report.ScanReportToSecHubResultTransformer;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

@Component
public class SerecoReportToSecHubResultTransformer implements ScanReportToSecHubResultTransformer {

    @Autowired
    SerecoFalsePositiveMarker falsePositiveMarker;

    @Value("${sechub.feature.showProductResultLink:false}")
    @MustBeDocumented(scope = "administration", value = "Administrators can turn on this mode to allow product links in json and HTML output")
    boolean showProductLineResultLink;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoReportToSecHubResultTransformer.class);

    @Override
    public SecHubResult transform(ProductResult productResult) throws SecHubExecutionException {
        String origin = productResult.getResult();
        String projectId = productResult.getProjectId();

        SerecoMetaData data = JSONConverter.get().fromJSON(SerecoMetaData.class, origin);

        falsePositiveMarker.markFalsePositives(projectId, data.getVulnerabilities());

        SecHubResult result = new SecHubResult();

        List<SecHubFinding> findings = result.getFindings();

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
            handleClassifications(finding, v, productResult.getSecHubJobUUID());
            
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

        return result;
    }

    private void handleClassifications(SecHubFinding finding, SerecoVulnerability v, UUID jobUUID) {
        SerecoClassification clazz = v.getClassification();
        String cwe = clazz.getCwe();
        if (cwe != null) {
            try {
                int cweInt = Integer.parseInt(cwe);
                finding.setCweId(cweInt);
            }catch(NumberFormatException e) {
                LOG.error("CWE information not valid:{} inside result for job:{}",cwe, jobUUID);
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
