// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider;

import com.mercedesbenz.sechub.model.FindingNode;

final class RelevantPartColumnLabelProvider extends AbstractSecHubFindingNodeColumnLabelProvider {
	@Override
	public String getTextForNode(FindingNode node) {
		return node.getRelevantPart();
	}
}