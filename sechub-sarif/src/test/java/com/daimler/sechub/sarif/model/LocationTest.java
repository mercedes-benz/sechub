// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LocationTest {

    @Test
    void constructor_with_params_null() {
        /* prepare */
        Location location = new Location(null);

        /* execute */
        PhysicalLocation physicalLocation = location.getPhysicalLocation();

        /* test */
        assertEquals(physicalLocation, null);
    }

    @Test
    void constructor_with_params_not_null() {
        /* prepare */
        Location location = new Location(createPhysicalLocation());

        /* execute */
        PhysicalLocation physicalLocation = location.getPhysicalLocation();

        /* test */
        assertEquals(physicalLocation, createPhysicalLocation());
    }

    @Test
    void test_setters() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode( createExample(), createExample());
        
        testBothAreNOTEqual( createExample(), change(createExample(), (location) -> location.setPhysicalLocation(change(createPhysicalLocation(),(physicalLocation) -> physicalLocation.setRegion(new Region(42,42))))));
        /* @formatter:on */

    }

    private Location createExample() {
        Location location = new Location();
        location.setPhysicalLocation(createPhysicalLocation());
        return location;
    }

    private PhysicalLocation createPhysicalLocation() {
        PhysicalLocation physicalLocation = new PhysicalLocation();
        return physicalLocation;
    }
}
