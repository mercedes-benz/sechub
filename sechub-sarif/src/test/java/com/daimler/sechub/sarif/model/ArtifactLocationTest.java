// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.PojoTester;

class ArtifactLocationTest {

    @Test
    void constructor_with_params_null() {
        /* prepare */
        ArtifactLocation artifactLocation = new ArtifactLocation(null, null);

        /* execute */
        String uri = artifactLocation.getUri();
        String uriBaseId = artifactLocation.getUriBaseId();

        /* test */
        assertEquals(uri, null);
        assertEquals(uriBaseId, null);
    }

    @Test
    void constructor_with_params_not_null() {
        /* prepare */
        ArtifactLocation artifactLocation = new ArtifactLocation("file:///home/user/directory/", "path/to/fileWithFinding.txt");

        /* execute */
        String uri = artifactLocation.getUri();
        String uriBaseId = artifactLocation.getUriBaseId();

        /* test */
        assertEquals(uri, "path/to/fileWithFinding.txt");
        assertEquals(uriBaseId, "file:///home/user/directory/");
    }

    @Test
    void test_setters() {
        PojoTester.testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode( createExample(), createExample());
        
        testBothAreNOTEqual( createExample(), change(createExample(), (location) -> location.setUriBaseId("other") ));
        testBothAreNOTEqual( createExample(), change(createExample(), (location) -> location.setUri("other") ));
        /* @formatter:on */

    }

    private ArtifactLocation createExample() {
        return new ArtifactLocation();
    }

}
