package com.daimler.sechub.sarif.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArtifactLocationTest {

    @Test
    void values_are_null() {
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
    void values_are_not_null() {
        /* prepare */
        ArtifactLocation artifactLocation = new ArtifactLocation("file:///home/user/directory/",
                "path/to/fileWithFinding.txt");

        /* execute */
        String uri = artifactLocation.getUri();
        String uriBaseId = artifactLocation.getUriBaseId();

        /* test */
        assertEquals(uri, "path/to/fileWithFinding.txt");
        assertEquals(uriBaseId, "file:///home/user/directory/");
    }

    @Test
    void test_setters() {
        /* prepare */
        ArtifactLocation artifactLocation = new ArtifactLocation();
        String expectedUriBaseId = "file:///home/user/directory/";
        String expectedUri = "path/to/fileWithFinding.txt";

        /* execute */
        artifactLocation.setUriBaseId("file:///home/user/directory/");
        artifactLocation.setUri("path/to/fileWithFinding.txt");

        /* test */
        assertEquals(expectedUriBaseId, artifactLocation.getUriBaseId());
        assertEquals(expectedUri, artifactLocation.getUri());
    }

}
