package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

//SPDX-License-Identifier: MIT
class SkopeoLocationConverterTest {

    private SkopeoLocationConverter converterToTest;

    @BeforeEach
    void beforeEach() {
        converterToTest = new SkopeoLocationConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://ubuntu:22.04", "http://ubuntu:22.04", "docker://ubuntu:22.04" })
    void convertLocationToDockerDownloadURL_docker_prefix_always_there(String location) {
        /* execute */
        String result = converterToTest.convertLocationToDockerDownloadURL(location);

        /* test */
        assertEquals("docker://ubuntu:22.04", result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://ubuntu:22.04", "http://ubuntu:22.04", "docker://ubuntu:22.04", "http://ubuntu:22.04" })
    void convertLocationForLogin_prefix_removed(String location) {
        /* execute */
        String result = converterToTest.convertLocationToLoginLocation(location);

        /* test */
        assertEquals("ubuntu:22.04", result);
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void convertLocationForLogin_wrong_location_just_return_origin(String wrongLocation) {
        /* execute */
        String result = converterToTest.convertLocationToLoginLocation(wrongLocation);

        /* test */
        assertEquals(wrongLocation, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://ubuntu:22.04", "http://ubuntu:22.04", "docker://ubuntu:22.04", "http://ubuntu:22.04" })
    void convertLocationForAdditionalTag_prefix_removed(String location) {
        /* execute */
        String result = converterToTest.convertLocationToAdditionalTag(location);

        /* test */
        assertEquals("ubuntu:22.04", result);
    }

    @Test
    void convertLocationForAdditionalTag_empty_location_default_tag_prefix_used() {
        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            converterToTest.convertLocationToAdditionalTag("");
        });

        /* test */
        assertTrue(exception.getMessage().contains("Could not set additional tag for skopeo location."));
    }

    @Test
    void convertLocationForAdditionalTag_with_path_as_tag() {
        /* execute */
        String result = converterToTest.convertLocationToAdditionalTag("artifacts.mycompany.com/artifacts/myimage:tag");

        /* test */
        assertEquals("artifacts.mycompany.com/artifacts/myimage:tag", result);
    }

}
