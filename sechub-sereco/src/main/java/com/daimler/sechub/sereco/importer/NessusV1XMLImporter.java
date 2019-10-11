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

import com.daimler.sechub.sereco.metadata.MetaData;
import com.daimler.sechub.sereco.metadata.Severity;
import com.daimler.sechub.sereco.metadata.Vulnerability;

@Component
public class NessusV1XMLImporter extends AbstractProductResultImporter {

	private static final Pattern NAME_PATTERN = Pattern.compile("_");

	public MetaData importResult(String xml) throws IOException {
		if (xml == null) {
			xml = "";
		}
		Document document;
		try {
			document = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			throw new IOException("Import cannot parse xml", e);
		}

		MetaData metaData = new MetaData();
		Element checkmarxCxXMLResults = document.getRootElement();
		Element reportElement = checkmarxCxXMLResults.element("Report");
		Element reportHost = reportElement.element("ReportHost");
		List<Element> reportItems = reportHost.elements("ReportItem");

		for (Element reportItem : reportItems) {
			String name = reportItem.attributeValue("svc_name");
			String type = NAME_PATTERN.matcher(name).replaceAll(" ");
			String output = reportItem.elementText("plugin_output");

			Vulnerability vulnerability = new Vulnerability();
			Severity severity = null;
			int severityLevel = Integer.parseInt(reportItem.attributeValue("severity"));
			if (severityLevel == 0) {
				severity = Severity.INFO;
			}else if (severityLevel==1) {
				severity = Severity.LOW;
			}else if (severityLevel==2){
				severity = Severity.MEDIUM;
			}else {
				severity = Severity.MEDIUM;
			}
			vulnerability.setSeverity(severity);
			vulnerability.setType(type);
			vulnerability.setDescription(output);
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
