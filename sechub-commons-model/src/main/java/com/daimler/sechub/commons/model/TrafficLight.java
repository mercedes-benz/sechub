// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

/**
 * A simple type representing "GREEN", "YELLOW", "RED"
 * 
 * @author Albert Tregnaghi
 *
 */
public enum TrafficLight {

	GREEN,

	YELLOW,

	RED;

	/**
	 * Tries to identify traffic light from string.
	 * 
	 * @param trafficLightString
	 * @return {@link TrafficLight} or <code>null</code> if not matching
	 */
	public static TrafficLight fromString(String trafficLightString) {
		if (trafficLightString == null) {
			return null;
		}
		for (TrafficLight light : values()) {
			if (light.name().equals(trafficLightString)) {
				return light;
			}
		}
		return null;
	}
}
