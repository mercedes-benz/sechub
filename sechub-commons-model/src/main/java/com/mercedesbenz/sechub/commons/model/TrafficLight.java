// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple type representing "GREEN", "YELLOW", "RED"
 *
 * @author Albert Tregnaghi
 *
 */
public enum TrafficLight {

    GREEN("Green", Severity.LOW, Severity.UNCLASSIFIED, Severity.INFO),

    YELLOW("Yellow", Severity.MEDIUM),

    RED("Red", Severity.CRITICAL, Severity.HIGH),

    /* Traffic light is "turned off" */
    OFF("Off");

    private List<Severity> severities;
    private String text;

    private TrafficLight(String text, Severity... severities) {
        this.text = text;

        List<Severity> target = new ArrayList<>(3);
        target.addAll(Arrays.asList(severities));

        this.severities = Collections.unmodifiableList(target);
    }

    public String getText() {
        return text;
    }

    /**
     * @return severities which are represented by this traffic light
     */
    public List<Severity> getSeverities() {
        return severities;
    }

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
        String upperCasedTrafficLightString = trafficLightString.toUpperCase();
        for (TrafficLight light : values()) {
            if (light.name().equals(upperCasedTrafficLightString)) {
                return light;
            }
        }
        return null;
    }
}
