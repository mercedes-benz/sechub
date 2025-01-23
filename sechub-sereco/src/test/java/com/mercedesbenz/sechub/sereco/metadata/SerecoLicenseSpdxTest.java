// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sereco.test.TestSerecoFileSupport;

public class SerecoLicenseSpdxTest {
    private static String spdx_2_2_scancode_json;
    private static String spdx_2_2_scancode_tag_value;
    private static String spdx_2_2_scancode_rdf;
    private static String spdx_2_2_scancode_yaml;

    @BeforeAll
    public static void before() {
        spdx_2_2_scancode_json = loadSpdxTestFile("spdx_scancode_30.1.0.spdx.json");
        spdx_2_2_scancode_tag_value = loadSpdxTestFile("spdx_scancode_30.1.0.spdx");
        spdx_2_2_scancode_rdf = loadSpdxTestFile("spdx_scancode_30.1.0.spdx.rdf");
        spdx_2_2_scancode_yaml = loadSpdxTestFile("spdx_scancode_30.1.0.spdx.yaml");
    }

    @Test
    void of_null_throws_exception() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> {
            SerecoLicenseSpdx.of(null);
        });
    }

    @Test
    void of_empty_string_throws_exception() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> {
            SerecoLicenseSpdx.of("");
        });
    }

    @Test
    void of_spdx_json_can_be_created() {
        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(spdx_2_2_scancode_json);

        /* test */
        assertTrue(spdx.hasJson());
        assertEquals(spdx_2_2_scancode_json, spdx.getJson());
    }

    @Test
    void of_spdx_tag_value_can_be_created() {

        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(spdx_2_2_scancode_tag_value);

        /* test */
        assertTrue(spdx.hasTagValue());
        assertEquals(spdx_2_2_scancode_tag_value, spdx.getTagValue());
    }

    @Test
    void of_spdx_yaml_can_be_created() {
        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(spdx_2_2_scancode_yaml);

        /* test */
        assertTrue(spdx.hasYaml());
        assertEquals(spdx_2_2_scancode_yaml, spdx.getYaml());
    }

    @Test
    void of_spdx_rdf_can_be_created() {
        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(spdx_2_2_scancode_rdf);

        /* test */
        assertTrue(spdx.hasRdf());
        assertEquals(spdx_2_2_scancode_rdf, spdx.getRdf());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private static String loadSpdxTestFile(String spdxTestFile) {
        return TestSerecoFileSupport.INSTANCE.loadTestFile("spdx/" + spdxTestFile);
    }
}
