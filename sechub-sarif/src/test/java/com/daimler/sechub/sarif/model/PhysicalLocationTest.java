// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.Assert.*;

import org.junit.jupiter.api.Test;

class PhysicalLocationTest {

    private static final String DEFAULT_LOCATION_URI = "uri1";
    private static final String DEFAULT_LOCATOINURL_BASE_ID = "urlBaseId1";

    @Test
    void constructor_params_are_null() {
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
    void constructor_params_not_null() {
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
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (location) -> location.setArtifactLocation(change(createArtifactLocation(),(artifactLocation)->artifactLocation.setUri("other")))));
        testBothAreNOTEqual(createExample(), change(createExample(), (location) -> location.setArtifactLocation(change(createArtifactLocation(),(artifactLocation)->artifactLocation.setUriBaseId("otherBase")))));
        /* @formatter:on */

    }

    private PhysicalLocation createExample() {
        PhysicalLocation location = new PhysicalLocation();

        ArtifactLocation artifactLocation = createArtifactLocation();
        location.setArtifactLocation(artifactLocation);

        return location;
    }

    private ArtifactLocation createArtifactLocation() {
        return new ArtifactLocation(DEFAULT_LOCATOINURL_BASE_ID, DEFAULT_LOCATION_URI);
    }

}
