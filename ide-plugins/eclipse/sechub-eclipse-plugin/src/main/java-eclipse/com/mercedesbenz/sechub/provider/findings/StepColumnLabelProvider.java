// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.findings;

import com.mercedesbenz.sechub.model.FindingNode;

final class StepColumnLabelProvider extends AbstractSecHubFindingNodeColumnLabelProvider {
	@Override
	public String getTextForNode(FindingNode node) {
		return "" + node.getCallStackStep();
	}
}