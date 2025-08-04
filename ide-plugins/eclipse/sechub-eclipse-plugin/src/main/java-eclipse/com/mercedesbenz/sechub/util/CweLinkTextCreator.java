// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.util;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.model.FindingNode;

public class CweLinkTextCreator {

	public static String createCweLinkTextWithInfos(FindingNode node) {
		if (node==null) {
			return "";
		}
		SecHubFinding finding = node.getFinding();
		if (finding==null) {
			return "";
		}
		String headDescription = " Finding " + finding.getId() + " - " + finding.getName();
		if (finding.getCweId() != null) {
			headDescription += " - <a href=\"https://cwe.mitre.org/data/definitions/" + finding.getCweId()
			+ ".html\">CWE-" + finding.getCweId() + "</a>";
		}
		headDescription += " ("+finding.getSeverity()+")";
		headDescription += "\n " + node.getJobUUID();
		return headDescription;
	}
}
