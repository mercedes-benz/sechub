// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.SecHubCodeCallStack;
import com.daimler.sechub.domain.scan.SecHubFinding;
import com.daimler.sechub.domain.scan.SecHubResult;
import com.daimler.sechub.domain.scan.Severity;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.report.ScanReportToSecHubResultTransformer;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.util.JSONConverter;

@Component
public class SerecoReportToSecHubResultTransformer implements ScanReportToSecHubResultTransformer {


	@Value("${sechub.feature.showProductResultLink:false}")
	@MustBeDocumented(scope="administration",value="Administrators can turn on this mode to allow product links in json and HTML output")
	boolean showProductLineResultLink;

	private static final Logger LOG = LoggerFactory.getLogger(SerecoReportToSecHubResultTransformer.class);

	@Override
	public SecHubResult transform(String origin) throws SecHubExecutionException {
		SerecoMetaData data = JSONConverter.get().fromJSON(SerecoMetaData.class, origin);
		SecHubResult result = new SecHubResult();

		List<SecHubFinding> findings = result.getFindings();
		int id = 1;
		for (SerecoVulnerability v : data.getVulnerabilities()) {
			SecHubFinding finding = new SecHubFinding();
			finding.setDescription(v.getDescription());
			finding.setName(v.getType());
			finding.setId(id++);
			finding.setSeverity(transformSeverity(v.getSeverity()));

			if(showProductLineResultLink) {
				finding.setProductResultLink(v.getProductResultLink());
			}
			finding.setCode(convert(v.getCode()));
			finding.setType(v.getScanType());

			findings.add(finding);
		}

		return result;
	}


	private SecHubCodeCallStack convert(SerecoCodeCallStackElement element) {
		if (element==null) {
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
		if (metaSeverity==null) {
			LOG.error("Missing Sereco Severity cannot transformed {} to sechub result! So returning unclassified!",metaSeverity);
			return Severity.UNCLASSIFIED;
		}
		for (Severity severity : Severity.values()) {
			if (severity.name().equals(metaSeverity.name())) {
				return severity;
			}
		}
		LOG.error("Was not able to tranform Sereco Severity:{} to sechub result! So returning unclassified!",metaSeverity);
		return Severity.UNCLASSIFIED;
	}

	@Override
	public boolean canTransform(ProductIdentifier productIdentifier) {
		return ProductIdentifier.SERECO.equals(productIdentifier);
	}

}
