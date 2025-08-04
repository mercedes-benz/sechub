// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;
import com.mercedesbenz.sechub.util.TrafficLightImageResolver;

public class TrafficLightLabelProvider extends ColumnLabelProvider {
	
	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public Image getImage(Object element) {
		
		TrafficLight trafficLight= null;
		
		if (element instanceof SecHubJobInfoForUser info) {
			trafficLight = info.getTrafficLight();
		}
		return TrafficLightImageResolver.resolveImage(trafficLight);
	}

}