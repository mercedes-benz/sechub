package com.daimler.sechub.sarif.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.daimler.sechub.test.PojoTester;

class LocationTest {

    @Test
    void value_is_null() {
        /* prepare */
        Location location = new Location(null);

        /* execute */
        PhysicalLocation physicalLocation = location.getPhysicalLocation();

        /* test */
        assertEquals(physicalLocation, null);
    }

    @Test
    void value_is_not_null() {
        /* prepare */
        Location location = new Location(new PhysicalLocation());

        /* execute */
        PhysicalLocation physicalLocation = location.getPhysicalLocation();

        /* test */
        assertEquals(physicalLocation, new PhysicalLocation());
    }

    @Test
    void test_setter() {
        /* prepare */
        Location location = new Location();

        /* execute + test */
        PojoTester.testSetterAndGetter(location);
    }

}
