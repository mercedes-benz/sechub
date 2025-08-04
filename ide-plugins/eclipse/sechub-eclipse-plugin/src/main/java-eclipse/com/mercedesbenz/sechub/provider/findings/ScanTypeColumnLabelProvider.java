// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.findings;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.util.ScanTypeImageResolver;

final class ScanTypeColumnLabelProvider extends ColumnLabelProvider {
	
	
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof FindingNode node) {
			SecHubFinding finding = node.getFinding();
			if (finding==null) {
				return null;
			}
			ScanType scanType = finding.getType();
			if (scanType==null) {
				return null;
			}
			return "Scan type: '"+scanType.getValue()+"'";
		}
		return null;
	}
	
	@Override
	public String getText(Object element) {
		return null;
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof FindingNode node) {
			SecHubFinding finding = node.getFinding();
			if (finding==null) {
				return null;
			}
			ScanType scanType = finding.getType();
			if (scanType==null) {
				return null;
			}
			return ScanTypeImageResolver.resolveImage(scanType);
			
			
		}
		return null;
	}
	
}