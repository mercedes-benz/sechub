// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.findings;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.model.FindingNodeFalsePositiveInfo;
import com.mercedesbenz.sechub.util.EclipseUtil;

final class FalsePositiveFindingNodeColumnLabelProvider extends ColumnLabelProvider {

	private static Image IMAGE_FALSE_POSITIVE = EclipseUtil.getImage("/icons/false_positive_marked.png");

	@Override
	public Image getImage(Object element) {
		if (element instanceof FindingNode) {
			return getImageForNode((FindingNode) element);
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof FindingNode) {
			FindingNode node = (FindingNode) element;
			FindingNodeFalsePositiveInfo jobData = node.getFalsePositiveInfo();
			if (jobData == null) {
				return null;
			}
			
			return """
					[%s] %s :
					
					%s""".formatted(jobData.getCreated(), jobData.getAuthor(), jobData.getComment());
			
		}
		return super.getToolTipText(element);
	}

	private Image getImageForNode(FindingNode element) {
		SecHubFinding finding = element.getFinding();
		if (finding == null) {
			return null;
		}
		ScanType type = finding.getType();
		if (type == null) {
			return null;
		}
		if (ScanType.WEB_SCAN.equals(type)) {
			return null;
		}
		if (element.isFalsePositive()) {
			return IMAGE_FALSE_POSITIVE;
		}
		return null;
	}

}