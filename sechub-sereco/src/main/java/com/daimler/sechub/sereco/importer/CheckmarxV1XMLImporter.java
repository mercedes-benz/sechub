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
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class CheckmarxV1XMLImporter extends AbstractProductResultImporter {

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

		CheckmarxCategoriesToClassificationConverter categoryConverter = new CheckmarxCategoriesToClassificationConverter();

		SerecoMetaData metaData = new SerecoMetaData();
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
				String deeplink = resultElement.attributeValue("DeepLink");
				String severity = resultElement.attributeValue("Severity");

				SerecoVulnerability vulnerability = new SerecoVulnerability();
				vulnerability.setFalsePositive(Boolean.parseBoolean(falsePositive));
				if (vulnerability.isFalsePositive()) {
				    vulnerability.setFalsePositiveReason("marked directly in security product");
				}
				vulnerability.setType(type);
				if ("Information".equalsIgnoreCase(severity)) {
					severity = "info";
				}
				vulnerability.setSeverity(SerecoSeverity.fromString(severity));

				SerecoCodeCallStackElement codeInfo = resolveCodeInfoFromElement(resultElement);

				vulnerability.setCode(codeInfo);
				vulnerability.setProductResultLink(deeplink);
				vulnerability.setDescription(""); // at least at the moment we set no description any more
				vulnerability.getClassification().setCwe(cweId);
				vulnerability.setScanType(ScanType.CODE_SCAN);

				categoryConverter.convert(categories, vulnerability.getClassification());

				metaData.getVulnerabilities().add(vulnerability);
			}
		}
		return metaData;
	}

	private SerecoCodeCallStackElement resolveCodeInfoFromElement(Element resultElement) {
		Element path = resultElement.element("Path");
		if (path == null) {
			return null;
		}
		List<Element> pathNodes = path.elements("PathNode");
		SerecoCodeCallStackElement initialCodeInfo=null;
		SerecoCodeCallStackElement infoBefore=null;
		for (Element pathNode: pathNodes) {
			SerecoCodeCallStackElement info = new SerecoCodeCallStackElement();
			if (initialCodeInfo==null) {
				initialCodeInfo=info;
			}
			fillPathNodeInfo(info,pathNode);
			if (infoBefore!=null) {
				infoBefore.setCalls(info);
			}
			infoBefore=info;
		}
		return initialCodeInfo;

	}

	private void fillPathNodeInfo(SerecoCodeCallStackElement info, Element pathNode) {

		Element filename = pathNode.element("FileName");
		if (filename != null) {
			info.setLocation(filename.getStringValue());
		}
		Element line = pathNode.element("Line");
		if (line != null) {
			info.setLine(safeGetInteger(line));
		}
		Element column = pathNode.element("Column");
		if (column != null) {
			info.setColumn(safeGetInteger(column));
		}

		Element name = pathNode.element("Name");
		if (name != null) {
			info.setRelevantPart(name.getStringValue());
		}

		addSource(info, pathNode);

	}

	private void addSource(SerecoCodeCallStackElement info, Element pathNode) {
		if (pathNode==null) {
			return;
		}
		/* add source snippet */
		Element snippet = pathNode.element("Snippet");
		if (snippet == null) {
			return ;
		}
		Element snippetLine = snippet.element("Line");
		if (snippetLine==null) {
			return;
		}
		Element snippetCode = snippetLine.element("Code");
		if (snippetCode == null) {
			return;
		}
		info.setSource(snippetCode.getStringValue());
	}

	/**
	 * Parses element string value and tries to resolve as integer
	 * @param element
	 * @return integer value or <code>null</code>
	 */
	Integer safeGetInteger(Element element) {
		if (element==null) {
			return null;
		}
		String string = element.getStringValue();
		if (string==null) {
			return null;
		}
		try {
			return Integer.valueOf(string);
		}catch(NumberFormatException e) {
			return null;
		}
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
