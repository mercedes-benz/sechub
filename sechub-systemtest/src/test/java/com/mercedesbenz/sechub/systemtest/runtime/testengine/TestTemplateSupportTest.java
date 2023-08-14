// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.systemtest.runtime.testengine.TestTemplateSupport.TemplateMatchResult;

class TestTemplateSupportTest {

    private TestTemplateSupport supportToTest;

    @BeforeEach
    void beforeEach() {
        supportToTest = new TestTemplateSupport();
    }

    @ParameterizedTest
    @ValueSource(strings = { "b40e014a-f94f-4e53-a930-e0d515624618", "09f0e4fc-160c-4729-8246-0b93d32598dd" })
    void sechub_job_uuid_placeholder_matches_template_data(String sechubJobUUID) {
        /* prepare */
        UUID uuid = UUID.fromString(sechubJobUUID);
        supportToTest.setSecHubJobUUID(uuid);

        /* execute + test */
        assertTrue(supportToTest.calculateTemplateMatching("abcd {sechub.jobuuid}", "abcd " + sechubJobUUID).isMatching());
    }

    @Test
    void sechub_job_uuid_not_set_means_not_matching() {
        /* prepare */
        supportToTest.setSecHubJobUUID(null);

        /* execute + test */
        assertFalse(supportToTest.calculateTemplateMatching("abcd {sechub.jobuuid}", "abcd null").isMatching());
        assertFalse(supportToTest.calculateTemplateMatching("abcd {sechub.jobuuid}", "abcd ").isMatching());
        assertTrue(supportToTest.calculateTemplateMatching("abcd {sechub.jobuuid}", "abcd {sechub.jobuuid}").isMatching());
    }

    @Test
    void star_placeholder_with_number_matches_template_data() {
        assertTrue(supportToTest.calculateTemplateMatching("abcd {*:36}", "abcd 25877f25-1f09-4281-8f3d-66cd0013b208").isMatching());
        assertTrue(supportToTest.calculateTemplateMatching("abcd {*:36} xyz", "abcd 25877f25-1f09-4281-8f3d-66cd0013b208 xyz").isMatching());
    }

    @Test
    void star_placeholder_with_number_matches_not_template_data() {
        assertFalse(supportToTest.calculateTemplateMatching("abcd {*:35}", "abcd 25877f25-1f09-4281-8f3d-66cd0013b208").isMatching());
        assertFalse(supportToTest.calculateTemplateMatching("abcd {*:37}", "abcd 25877f25-1f09-4281-8f3d-66cd0013b208").isMatching());
        assertFalse(supportToTest.calculateTemplateMatching("abcd {*:37}", null).isMatching());
    }

    @Test
    void complex_example_internal_test() {
        /* prepare */
        String content = """
                  status" : "SUCCESS",
                  "reportVersion" : "1.0",
                  "messages" : [ {
                    "type" : "ERROR",
                    "text" : "error for PDS job: 267906f0-a0cc-4a6e-a4f3-8cee9d3618f0 but with\n    a multi line ....\n    "
                  }, {
                    "type" : "WARNING",
                    "text" : "warn for PDS job: 267906f0-a0cc-4a6e-a4f3-8cee9d3618f0"
                  }, {
                    "type" : "INFO",
                    "text" : "info for PDS job: 267906f0-a0cc-4a6e-a4f3-8cee9d3618f0"
                  } ]



                """;

        String template = """


                  status" :               "SUCCESS",
                  "reportVersion" : "1.0",
                  "messages" : [ {
                    "type" : "ERROR",
                    "text" : "error for PDS job: {*:36} but with\n    a multi line ....\n    "
                  }, {
                    "type" : "WARNING",
                    "text" : "warn for PDS job: {*:36}"
                  }, {
                    "type" : "INFO",
                    "text" : "info for PDS job: {*:36}"
                  } ]
                """;

        /* execute */
        TemplateMatchResult data = supportToTest.calculateTemplateMatching(template, content);

        /* test */
        assertEquals(data.getTransformedTemplate(), data.getTransformedContent(), "changed template <!=> changed content");
    }

    @ParameterizedTest
    @ValueSource(strings = { "{*:x}", "{*:}", "{*}", "{**}", "{*:*}" })
    void wrong_configured_placeholders_throw_test_template_exception(String placeholder) {
        /* execute */
        TestTemplateException exception = assertThrows(TestTemplateException.class,
                () -> supportToTest.calculateTemplateMatching("abcd " + placeholder, "abcd 25877f25-1f09-4281-8f3d-66cd0013b208"));

        /* test */
        assertTrue(exception.getMessage().contains("Star placeholder syntax is {*:$amountOfCharsToIgnore}"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{unknown}", "{blubb:1}", "{}", "{_*}", "{x:1}" })
    void unknown_placeholders_are_just_ignored(String placeholder) {
        assertFalse(supportToTest.calculateTemplateMatching("abcd " + placeholder, "abcd something else").isMatching());
        assertTrue(supportToTest.calculateTemplateMatching("abcd " + placeholder, "abcd " + placeholder).isMatching());
    }

    @Test
    void template_null_throws_illegal_argument_exception() {
        assertThrows(IllegalArgumentException.class, () -> supportToTest.calculateTemplateMatching(null, "content"));
    }

}
