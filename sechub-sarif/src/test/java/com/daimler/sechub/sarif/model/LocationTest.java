package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class LocationTest {

    @Test
    public void value_is_null() {
        /* prepare */
        Location location = new Location(null);

        /* execute */
        PhysicalLocation physicalLocation = location.getPhysicalLocation();

        /* test */
        assertEquals(physicalLocation, null);
    }

    @Test
    public void value_is_not_null() {
        /* prepare */
        Location location = new Location(new PhysicalLocation());

        /* execute */
        PhysicalLocation physicalLocation = location.getPhysicalLocation();

        /* test */
        assertEquals(physicalLocation, new PhysicalLocation());
    }

    @Test
    public void test_setter() {
        /* prepare */
        Location location = new Location();

        /* execute */
        location.setPhysicalLocation(new PhysicalLocation());

        /* test */
        assertEquals(location.getPhysicalLocation(), new PhysicalLocation());
    }

}
