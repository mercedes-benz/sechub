package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.PojoTester;

class PhysicalLocationTest {

    @Test
    void value_is_null() {
        /* prepare */
        PhysicalLocation physicalLocation = new PhysicalLocation(null, null);

        /* execute */
        ArtifactLocation artifactLocation = physicalLocation.getArtifactLocation();
        Region region = physicalLocation.getRegion();

        /* test */
        assertEquals(artifactLocation, null);
        assertEquals(region, null);
    }

    @Test
    void value_is_not_null() {
        /* prepare */
        PhysicalLocation physicalLocation = new PhysicalLocation(new ArtifactLocation(), new Region());

        /* execute */
        ArtifactLocation artifactLocation = physicalLocation.getArtifactLocation();
        Region region = physicalLocation.getRegion();

        /* test */
        assertEquals(artifactLocation, new ArtifactLocation());
        assertEquals(region, new Region());
    }

    @Test
    void test_setter() {
        /* prepare */
        PhysicalLocation physicalLocation = new PhysicalLocation();

        /* execute + test */
        PojoTester.testSetterAndGetter(physicalLocation);
    }

}
