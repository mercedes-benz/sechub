package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
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
    @ValueSource(strings = { "https://ubuntu:22.04", "http://ubuntu:22.04", "docker://ubuntu:22.04", "http://ubuntu:22.04" })
    void convertLocationForDownload_docker_prefix_always_there(String location) {
        /* execute */
        String result = converterToTest.convertLocationForDownload(location);

        /* test */
        assertEquals("docker://ubuntu:22.04", result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://ubuntu:22.04", "http://ubuntu:22.04", "docker://ubuntu:22.04", "http://ubuntu:22.04" })
    void convertLocationForLogin_prefix_removed(String location) {
        /* execute */
        String result = converterToTest.convertLocationForLogin(location);

        /* test */
        assertEquals("ubuntu:22.04", result);
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void convertLocationForLogin_wrong_location_just_return_origin(String wrongLocation) {
        /* execute */
        String result = converterToTest.convertLocationForLogin(wrongLocation);

        /* test */
        assertEquals(wrongLocation, result);
    }

}
