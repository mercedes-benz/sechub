// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sereco.metadata.MetaData;
import com.daimler.sechub.sereco.metadata.Severity;
import com.daimler.sechub.sereco.metadata.Vulnerability;

@Component
public class CheckmarxV1XMLImporter extends AbstractProductResultImporter {


	private static final Logger LOG = LoggerFactory.getLogger(CheckmarxV1XMLImporter.class);

	private static final Pattern NAME_PATTERN = Pattern.compile("_");

	@SuppressWarnings("unchecked")
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
		List<Element> queryElements = checkmarxCxXMLResults.elements("Query");

		for (Element queryElement : queryElements) {

			String name = queryElement.attributeValue("name");
			String type = NAME_PATTERN.matcher(name).replaceAll(" ");
			String categories = queryElement.attributeValue("categories");
			String cweId = queryElement.attributeValue("cweId");

			List<Element> resultElements = queryElement.elements("Result");
			for (Element resultElement : resultElements) {

				String falsePositive = resultElement.attributeValue("FalsePositive");
				if (Boolean.parseBoolean(falsePositive)) {
					String nodeId=resultElement.attributeValue("NodeId");
					LOG.debug("Ignored marked false positive for NodeId:{}",nodeId);
					continue;
				}
				String deeplink = resultElement.attributeValue("DeepLink");
				String severity = resultElement.attributeValue("Severity");
				String fileName = resultElement.attributeValue("FileName");
				String line = resultElement.attributeValue("Line");
				String column = resultElement.attributeValue("Column");

				Vulnerability vulnerability = new Vulnerability();
				vulnerability.setType(type);

				if ("Information".equalsIgnoreCase(severity)) {
					severity = "info";
				}
				vulnerability.setSeverity(Severity.fromString(severity));

				StringBuilder sb = new StringBuilder();
				sb.append("\n<br>Location:").append(fileName).append(" - line:").append(line).append(", column:")
						.append(column);
				sb.append("\\n<br>For details look at <a href='").append(deeplink).append("'>Full result</a>");
				vulnerability.setDescription(sb.toString());
				vulnerability.getClassification().setCwe(cweId);
				new CheckmarxCategoriesToClassificationConverter().convert(categories,
						vulnerability.getClassification());
				metaData.getVulnerabilities().add(vulnerability);
			}
		}
		return metaData;
	}

	@Override
	protected ImportSupport createImportSupport() {
		/* @formatter:off */
		return ImportSupport.
							builder().
								productId("Checkmarx").
								contentIdentifiedBy("CxXMLResults").
								mustBeXML().
								build();
		/* @formatter:on */
	}
}
