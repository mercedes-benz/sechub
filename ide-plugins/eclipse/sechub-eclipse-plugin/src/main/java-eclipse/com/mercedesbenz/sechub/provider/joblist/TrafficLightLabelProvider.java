// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;

public class TrafficLightLabelProvider extends ColumnLabelProvider {
	
	private static final String PATH = "icons/trafficlight_";
	private static final String PATH_GREEN= PATH+"green.png";
	private static final String PATH_YELLOW= PATH+"yellow.png";
	private static final String PATH_RED= PATH+"red.png";
	private static final String PATH_OFF= PATH+"off.png";
	ImageDescriptor descriptor = EclipseUtil.createDescriptor("icons/sechub-decorator.gif");

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public Image getImage(Object element) {
		String path = PATH_OFF;
		
		if (element instanceof SecHubJobInfoForUser info) {
			TrafficLight trafficLight = info.getTrafficLight();
			if (trafficLight!=null) {
				switch (trafficLight) {
				case GREEN:
					path = PATH_GREEN;
					break;
				case RED:
					path = PATH_RED;
					break;
				case YELLOW:
					path = PATH_YELLOW;
					break;
				default:
					break;
			}
			
			}
		}
		return EclipseUtil.getImage(path);
	}

}