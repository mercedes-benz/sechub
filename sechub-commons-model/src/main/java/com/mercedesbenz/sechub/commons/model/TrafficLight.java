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

    GREEN(Severity.LOW, Severity.UNCLASSIFIED, Severity.INFO),

    YELLOW(Severity.MEDIUM),

    RED(Severity.CRITICAL, Severity.HIGH),

    /* Traffic light is "turned off" */
    OFF;

    private List<Severity> severities;

    private TrafficLight(Severity... severities) {
        List<Severity> target = new ArrayList<>(3);
        target.addAll(Arrays.asList(severities));

        this.severities = Collections.unmodifiableList(target);
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
