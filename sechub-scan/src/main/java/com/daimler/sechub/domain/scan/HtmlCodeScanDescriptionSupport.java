// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;

/**
 * This class builds descriptions suitable for HTML output
 *
 * @author Albert Tregnaghi
 *
 */
public class HtmlCodeScanDescriptionSupport {

	public boolean isCodeScan(SecHubFinding finding) {
		SecHubCodeCallStack code = finding.getCode();
		return code != null;
	}

	public List<HTMLScanResultCodeScanEntry> buildEntries(SecHubFinding finding) {
		if (finding == null) {
			return Collections.emptyList();
		}
		SecHubCodeCallStack code = finding.getCode();
		if (code == null) {
			return Collections.emptyList();
		}
		SecHubCodeCallStack lastCode = code;
		while (lastCode.getCalls() != null) {
			lastCode = lastCode.getCalls();
		}

		List<HTMLScanResultCodeScanEntry> descriptionList = new ArrayList<>();
		HTMLScanResultCodeScanEntry entry1 = createEntry(code);

		descriptionList.add(entry1);

		if (lastCode != null) {
			/* create pseudo variant to show call hierarchy */
			HTMLScanResultCodeScanEntry pseudoEntry = new HTMLScanResultCodeScanEntry();
			pseudoEntry.location = "...";
			descriptionList.add(pseudoEntry);
			descriptionList.add(createEntry(lastCode));
		}

		return descriptionList;

	}

	private HTMLScanResultCodeScanEntry createEntry(SecHubCodeCallStack code) {
		Objects.nonNull(code);

		HTMLScanResultCodeScanEntry entry = new HTMLScanResultCodeScanEntry();

		entry.column = code.getColumn();
		entry.line = code.getLine();
		entry.location = code.getLocation();
		entry.relevantPart=code.getRelevantPart();

		String source = code.getSource();
		if (source != null) {
			entry.source = source.trim();// to improve HTML report readability we do trim leading spaces...
		}

		return entry;
	}
}
