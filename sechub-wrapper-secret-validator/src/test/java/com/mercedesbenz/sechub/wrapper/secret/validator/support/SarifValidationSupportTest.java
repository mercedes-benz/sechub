// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.jcup.sarif_2_1_0.model.Location;
import de.jcup.sarif_2_1_0.model.PhysicalLocation;
import de.jcup.sarif_2_1_0.model.Region;
import de.jcup.sarif_2_1_0.model.Result;

class SarifValidationSupportTest {

    private SarifValidationSupport supportToTest = new SarifValidationSupport();

    @Test
    void finding_null_returns_false() {
        /* execute */
        boolean findingCanBeValidated = supportToTest.findingCanBeValidated(null);

        /* test */
        assertFalse(findingCanBeValidated);
    }

    @Test
    void finding_locations_is_null_returns_false() {
        /* prepare */
        Result finding = new Result();
        finding.setLocations(null);

        /* execute */
        boolean findingCanBeValidated = supportToTest.findingCanBeValidated(finding);

        /* test */
        assertFalse(findingCanBeValidated);
    }

    @Test
    void finding_locations_is_empty_returns_false() {
        /* prepare */
        Result finding = new Result();
        finding.setLocations(new ArrayList<>());

        /* execute */
        boolean findingCanBeValidated = supportToTest.findingCanBeValidated(finding);

        /* test */
        assertFalse(findingCanBeValidated);
    }

    @Test
    void finding_locations_conatins_one_location_object_returns_true() {
        /* prepare */
        List<Location> locations = new ArrayList<>();
        locations.add(new Location());
        Result finding = new Result();
        finding.setLocations(locations);

        /* execute */
        boolean findingCanBeValidated = supportToTest.findingCanBeValidated(finding);

        /* test */
        assertTrue(findingCanBeValidated);
    }

    @Test
    void finding_location_is_null_returns_false() {
        /* execute */
        boolean findingCanBeValidated = supportToTest.findingLocationCanBeValidated(null);

        /* test */
        assertFalse(findingCanBeValidated);
    }

    @Test
    void finding_location_physical_location_is_null_returns_false() {
        /* prepare */
        Location location = new Location();
        location.setPhysicalLocation(null);

        /* execute */
        boolean findingCanBeValidated = supportToTest.findingLocationCanBeValidated(location);

        /* test */
        assertFalse(findingCanBeValidated);
    }

    @Test
    void finding_location_physical_location_region_is_null_returns_false() {
        /* prepare */
        PhysicalLocation physicalLocation = new PhysicalLocation();
        physicalLocation.setRegion(null);
        Location location = new Location();
        location.setPhysicalLocation(physicalLocation);

        /* execute */
        boolean findingCanBeValidated = supportToTest.findingLocationCanBeValidated(location);

        /* test */
        assertFalse(findingCanBeValidated);
    }

    @Test
    void finding_location_physical_location_region_is_not_null_returns_false() {
        /* prepare */
        PhysicalLocation physicalLocation = new PhysicalLocation();
        physicalLocation.setRegion(new Region());
        Location location = new Location();
        location.setPhysicalLocation(physicalLocation);

        /* execute */
        boolean findingCanBeValidated = supportToTest.findingLocationCanBeValidated(location);

        /* test */
        assertTrue(findingCanBeValidated);
    }

}
