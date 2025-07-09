// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.findings;

import com.mercedesbenz.sechub.api.internal.gen.model.Severity;
import com.mercedesbenz.sechub.model.FindingNode;

final class SeverityColumnLabelProvider extends AbstractSecHubFindingNodeColumnLabelProvider {
	@Override
	public String getTextForNode(FindingNode node) {
		Severity severity = node.getSeverity();
		if (severity == null) {
			return "";
		}
		return severity.toString();
	}
}