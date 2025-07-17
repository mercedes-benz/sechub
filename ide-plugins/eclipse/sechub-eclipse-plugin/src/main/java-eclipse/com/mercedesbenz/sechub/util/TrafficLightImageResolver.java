package com.mercedesbenz.sechub.util;

import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;

public class TrafficLightImageResolver {

	public static Image resolveImage(TrafficLight trafficLight) {
		String path = "icons/trafficlight_off.png";

		if (trafficLight != null) {
			switch (trafficLight) {
			case GREEN:
				path = "icons/trafficlight_green.png";
				break;
			case RED:
				path = "icons/trafficlight_red.png";
				break;
			case YELLOW:
				path = "icons/trafficlight_yellow.png";;
				break;
			default:
				break;
			}

		}
		return EclipseUtil.getImage(path);
	}
}
