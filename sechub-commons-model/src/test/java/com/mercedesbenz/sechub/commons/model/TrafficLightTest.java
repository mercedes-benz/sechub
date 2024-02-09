// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
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

    @Test
    void red_trafficlight_has_2_severities_critical_and_high() {

        List<Severity> severities = TrafficLight.RED.getSeverities();

        assertTrue(severities.contains(Severity.HIGH));
        assertTrue(severities.contains(Severity.CRITICAL));

        assertEquals(2, severities.size());
    }

    @Test
    void yellow_trafficlight_has_1_severitiy_medium() {

        List<Severity> severities = TrafficLight.YELLOW.getSeverities();

        assertTrue(severities.contains(Severity.MEDIUM));

        assertEquals(1, severities.size());
    }

    @Test
    void green_trafficlight_has_3_severities_low_unclassified_info() {

        List<Severity> severities = TrafficLight.GREEN.getSeverities();

        assertTrue(severities.contains(Severity.LOW));
        assertTrue(severities.contains(Severity.UNCLASSIFIED));
        assertTrue(severities.contains(Severity.INFO));

        assertEquals(3, severities.size());
    }

    @Test
    void off_trafficlight_has_no_severities() {

        List<Severity> severities = TrafficLight.OFF.getSeverities();

        assertEquals(0, severities.size());
    }

    /*
     * the test is more a sanity test - if somebody adds a traffic light field -
     * which should not happen ... but.. - this would check the field has at least
     * one severity.
     */
    @ParameterizedTest()
    @EnumSource(value = TrafficLight.class, mode = Mode.EXCLUDE, names = "OFF")
    void traffic_light_has_at_least_one_severity(TrafficLight light) {
        if (light.getSeverities().size() < 1) {
            fail("Traffic light " + light + " has an empty severities list! This may not happen");
        }
    }
    
    @Test
    void no_traffic_light_severity_cross_over() {
        List<Severity> severitiesAll = new ArrayList<>();
        for (TrafficLight trafficLight: TrafficLight.values()) {
            List<Severity> severities = trafficLight.getSeverities();
            for (Severity severity: severities) {
                if (severitiesAll.contains(severity)) {
                    fail("Severity cross over detected: "+ severity +" is defined in "+trafficLight+" but also in at least one other traffic light");
                }
                severitiesAll.add(severity);
            }
        }
    }
    
    @Test
    void traffic_lights_contain_all_severities() {
        Set<Severity> severitiesAll = new HashSet<>();
        for (TrafficLight trafficLight: TrafficLight.values()) {
            List<Severity> severities = trafficLight.getSeverities();
            for (Severity severity: severities) {
                severitiesAll.add(severity);
            }
        }
        assertEquals(Severity.values().length, severitiesAll.size());
    }
    

}
