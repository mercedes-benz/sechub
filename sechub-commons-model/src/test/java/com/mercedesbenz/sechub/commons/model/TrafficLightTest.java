// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class TrafficLightTest {

    @ParameterizedTest
    @EnumSource(TrafficLight.class)
    void fromString_values_are_supported(TrafficLight light) {
        assertEquals(light, TrafficLight.fromString(light.name()));
    }

    @ParameterizedTest
    @EnumSource(TrafficLight.class)
    void fromString_lowercased_values_are_supported(TrafficLight light) {
        assertEquals(light, TrafficLight.fromString(light.name().toLowerCase()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "unknown-illegal", "a", "." })
    @EmptySource
    @NullSource
    void fromString_unknown_values_returns_null(String string) {
        assertNull(TrafficLight.fromString(string));
    }
}
