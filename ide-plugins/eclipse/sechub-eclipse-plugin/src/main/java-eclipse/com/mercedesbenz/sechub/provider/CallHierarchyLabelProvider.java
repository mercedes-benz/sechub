// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.model.FindingNode;

public class CallHierarchyLabelProvider extends LabelProvider implements IStyledLabelProvider{

	private Image image;
	
	public CallHierarchyLabelProvider() {
		image = EclipseUtil.getImage("icons/activity.png");
	}
	
	@Override
	public StyledString getStyledText(Object element) {
		if (! (element instanceof FindingNode)) {
			return null;
		}
		FindingNode node = (FindingNode) element;
		StyledString str = new StyledString();
		
		StyledString relevantPartString = new StyledString(node.getRelevantPart());
		
		StyledString fileNameString = new StyledString(node.getFileName());
		fileNameString.setStyle(0, fileNameString.length(), StyledString.QUALIFIER_STYLER);
		
		str.append(relevantPartString);
		str.append(" - ");
		str.append(fileNameString);
		return str;
	}
	@Override
	public Image getImage(Object element) {
		return image;
	}
}