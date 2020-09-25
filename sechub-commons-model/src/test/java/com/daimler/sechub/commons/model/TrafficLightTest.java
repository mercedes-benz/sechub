// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class TrafficLightTest {

	@Test
	public void null_supported() {
		assertNull(TrafficLight.fromString(null));
	}

	@Test
	public void values_are_supported() {
		for (TrafficLight light : TrafficLight.values()) {
			assertEquals(light, TrafficLight.fromString(light.name()));
		}
	}

	@Test
	public void unknown_values_returns_null() {
		assertNull(TrafficLight.fromString("unknown-illegal"));
	}
}
