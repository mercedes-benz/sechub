// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class NessusV1XMLImporter extends AbstractProductResultImporter {

	private static final Pattern NAME_PATTERN = Pattern.compile("_");

	public SerecoMetaData importResult(String xml) throws IOException {
		if (xml == null) {
			xml = "";
		}
		Document document;
		try {
			document = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			throw new IOException("Import cannot parse xml", e);
		}

		SerecoMetaData metaData = new SerecoMetaData();
		Element checkmarxCxXMLResults = document.getRootElement();
		Element reportElement = checkmarxCxXMLResults.element("Report");
		Element reportHost = reportElement.element("ReportHost");
		List<Element> reportItems = reportHost.elements("ReportItem");

		for (Element reportItem : reportItems) {
			String name = reportItem.attributeValue("svc_name");
			String type = NAME_PATTERN.matcher(name).replaceAll(" ");
			String output = reportItem.elementText("plugin_output");
			String cve = reportItem.elementText("cve");
			
			SerecoVulnerability vulnerability = new SerecoVulnerability();
			SerecoSeverity severity = null;
			int severityLevel = Integer.parseInt(reportItem.attributeValue("severity"));
			if (severityLevel == 0) {
				severity = SerecoSeverity.INFO;
			}else if (severityLevel==1) {
				severity = SerecoSeverity.LOW;
			}else if (severityLevel==2){
				severity = SerecoSeverity.MEDIUM;
			}else {
				severity = SerecoSeverity.HIGH;
			}
			vulnerability.setSeverity(severity);
			vulnerability.setType(type);
			vulnerability.setDescription(output);
			vulnerability.setScanType(ScanType.INFRA_SCAN);
			vulnerability.getClassification().setCve(cve);
			metaData.getVulnerabilities().add(vulnerability);
		}
		return metaData;
	}

	@Override
	protected ImportSupport createImportSupport() {
		/* @formatter:off */
		return ImportSupport.
							builder().
								productId("Nessus").
								mustBeXML().
								contentIdentifiedBy("<NessusClientData_v2").
								build();
		/* @formatter:on */
	}
}
