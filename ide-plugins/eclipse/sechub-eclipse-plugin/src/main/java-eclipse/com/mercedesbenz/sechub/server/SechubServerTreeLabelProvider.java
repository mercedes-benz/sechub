// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.SecHubServerConnection;

public class SechubServerTreeLabelProvider extends BaseLabelProvider implements IStyledLabelProvider {
	private static Image IMAGE_SECHUB_SERVER = EclipseUtil.getImage("/icons/sechub-server.png");

	@Override
	public Image getImage(Object element) {
		if (element instanceof SecHubServerConnection) {
			return IMAGE_SECHUB_SERVER;
		}
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		StyledString string = new StyledString();
		if (element instanceof SecHubServerConnection) {
			SecHubServerConnection server = (SecHubServerConnection) element;
			string.append(server.getUrl());
		}else {
			string.append(Objects.toString(element));
		}
		return string;
	}

}
