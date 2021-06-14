package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class PhysicalLocationTest {

    @Test
    public void value_is_null() {
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
    public void value_is_not_null() {
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
    public void test_setter() {
        /* prepare */
        PhysicalLocation physicalLocation = new PhysicalLocation();

        /* execute */
        physicalLocation.setArtifactLocation(new ArtifactLocation());
        physicalLocation.setRegion(new Region());

        /* test */
        assertEquals(physicalLocation.getArtifactLocation(), new ArtifactLocation());
        assertEquals(physicalLocation.getRegion(), new Region());
    }

}
