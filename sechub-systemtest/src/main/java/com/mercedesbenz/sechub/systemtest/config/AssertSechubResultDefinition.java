// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;

public class AssertSechubResultDefinition extends AbstractDefinition {

    private Optional<TrafficLight> hasTrafficLight = Optional.ofNullable(null);
    private Optional<AssertEqualsFileDefinition> equalsFile = Optional.ofNullable(null);
    private Optional<AssertContainsStringsDefinition> containsStrings = Optional.ofNullable(null);

    public Optional<TrafficLight> getHasTrafficLight() {
        return hasTrafficLight;
    }

    public void setHasTrafficLight(Optional<TrafficLight> hasTrafficLight) {
        this.hasTrafficLight = hasTrafficLight;
    }

    public void setEqualsFile(Optional<AssertEqualsFileDefinition> equalsFile) {
        this.equalsFile = equalsFile;
    }

    public Optional<AssertEqualsFileDefinition> getEqualsFile() {
        return equalsFile;
    }

    public Optional<AssertContainsStringsDefinition> getContainsStrings() {
        return containsStrings;
    }

    public void setContainsStrings(Optional<AssertContainsStringsDefinition> containsStrings) {
        this.containsStrings = containsStrings;
    }
}
