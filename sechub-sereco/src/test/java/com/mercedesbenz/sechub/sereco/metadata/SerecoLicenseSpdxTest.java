package com.mercedesbenz.sechub.sereco.metadata;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sereco.test.SerecoTestFileSupport;

public class SerecoLicenseSpdxTest {
    private static String spdx_2_2_scancode;

    @BeforeAll
    public static void before() {
        spdx_2_2_scancode = loadSpdxTestFile("spdx_scancode_30.1.0.spdx.json");
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
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(spdx_2_2_scancode);

        /* test */
        assertTrue(spdx.hasJson());
        assertEquals(spdx_2_2_scancode, spdx.getJson());
    }

    @Test
    void of_spdx_tag_value_can_be_created() {
        /* prepare */
        String tagValue = "SPDXID: SPDXRef-DOCUMENT";

        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(tagValue);

        /* test */
        assertTrue(spdx.hasTagValue());
        assertEquals(tagValue, spdx.getTagValue());
    }

    @Test
    void of_spdx_yaml_can_be_created() {
        /* prepare */
        String yaml = "SPDXID: \"SPDXRef-DOCUMENT\"";

        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(yaml);

        /* test */
        assertTrue(spdx.hasYaml());
        assertEquals(yaml, spdx.getYaml());
    }

    @Test
    void of_spdx_rdf_can_be_created() {
        /* prepare */
        String rdf = "\"<rdf:RDF\"";

        /* execute */
        SerecoLicenseSpdx spdx = SerecoLicenseSpdx.of(rdf);

        /* test */
        assertTrue(spdx.hasRdf());
        assertEquals(rdf, spdx.getRdf());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private static String loadSpdxTestFile(String spdxTestFile) {
        return SerecoTestFileSupport.INSTANCE.loadTestFile("spdx/" + spdxTestFile);
    }
}
