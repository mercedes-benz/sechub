// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SpringValueExtractorTest {

    private SpringValueExtractor extractorToTest;

    @Before
    public void before() throws Exception {
        extractorToTest = new SpringValueExtractor();
    }

    @Test
    public void a_b_c_d_with_default_value_with_https_google_de_contains_default_value_https_gogole_de() {
        /* execute */
        String result = extractorToTest.extract("${a.b.c.d:https://www.google.de}").getDefaultValue();

        /* test */
        assertEquals("https://www.google.de", result);
    }

    @Test
    public void a_b_c_d_with_default_value_with_https_google_de_contains_key_a_b_c_d() {
        /* execute */
        String result = extractorToTest.extract("${a.b.c.d:https://www.google.de}").getKey();

        /* test */
        assertEquals("a.b.c.d", result);
    }

    @Test
    public void is_spring_value_returns_expected_values() {
        assertTrue(extractorToTest.isSpringValue("${a.bc.d}"));

        assertFalse(extractorToTest.isSpringValue(null));
        assertFalse(extractorToTest.isSpringValue(""));
        assertFalse(extractorToTest.isSpringValue("{a.bc.d}"));
        assertFalse(extractorToTest.isSpringValue("${a.bc.d"));
        assertFalse(extractorToTest.isSpringValue("$a.bc.d}"));
        assertFalse(extractorToTest.isSpringValue("a.bc.d"));
    }

    @Test
    public void toDescription_uses_key_and_default_value_when_set() throws Exception {

        /* execute */
        String result = extractorToTest.extract("${a.b.c.d:1234}").toDescription();

        /* test */
        assertEquals("Key:a.b.c.d, per default:1234", result);

    }

    @Test
    public void toDescription_uses_key_and_hint_about_not_optional_value_when_key_set_but_no_default() throws Exception {

        /* execute */
        String result = extractorToTest.extract("${a.b.c.d}").toDescription();

        /* test */
        assertEquals("Key:a.b.c.d, no default set so must be defined", result);

    }

    @Test
    public void a_b_c_d_colon_1_2_3_extracted() {
        /* execute */
        SpringValueExtractor.SpringValue extracted = extractorToTest.extract("${a.bc.d:123}");

        /* test */
        assertNotNull(extracted);
        assertEquals("a.bc.d", extracted.getKey());
        assertEquals("123", extracted.getDefaultValue());
    }

    @Test
    public void a_b_c_d_extracted() {
        /* execute */
        SpringValueExtractor.SpringValue extracted = extractorToTest.extract("${a.bc.d}");

        /* test */
        assertNotNull(extracted);
        assertEquals("a.bc.d", extracted.getKey());
        assertEquals("", extracted.getDefaultValue());
    }

}
