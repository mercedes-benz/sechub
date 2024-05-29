// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;

class PrepareWrapperRemoteConfigurationExtractorTest {

    PrepareWrapperRemoteConfigurationExtractor extractorToTest;

    @BeforeEach
    void beforeEach() {
        extractorToTest = new PrepareWrapperRemoteConfigurationExtractor();
    }

    @Test
    void extractor_returns_list_with_one_element_when_one_remote_section_is_configured() {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "remote": {
                          "location": "remote_example_location",
                          "type": "git"
                        }
                      }
                    ]
                  },
                  "codeScan": {
                    "use": [
                      "remote_example_name"
                    ]
                  }
                }
                """;
        SecHubConfigurationModel model = createFromJSON(json);

        /* execute */
        SecHubRemoteDataConfiguration result = extractorToTest.extract(model);

        /* test */
        assertEquals("remote_example_location", result.getLocation());
        assertEquals("git", result.getType());
    }

    @Test
    void extractor_returns_empty_list_when_sechub_model_is_empty() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubRemoteDataConfiguration result = extractorToTest.extract(model);

        /* test */
        assertNull(result);
    }

    @Test
    void extractor_throws_illegal_argument_exception_when_sechub_model_is_null() {

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> extractorToTest.extract(null));

        /* test */
        assertTrue(exception.getMessage().contains("Context was not initialized correctly. SecHub configuration was null"));
    }

    @Test
    void extractor_returns_list_with_three_elements_when_three_remote_sections_are_configured() {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "remote": {
                          "location": "remote_example_location",
                          "type": "git"
                        }
                      },
                                         {
                        "remote": {
                          "location": "remote_example_location2",
                          "type": "docker"
                        }
                      },
                                         {
                        "remote": {
                          "location": "remote_example_location3",
                          "type": ""
                        }
                      }
                    ]
                  },
                  "codeScan": {
                    "use": [
                      "remote_example_name"
                    ]
                  }
                }
                """;
        SecHubConfigurationModel model = createFromJSON(json);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> extractorToTest.extract(model));

        /* test */
        assertTrue(exception.getMessage().contains("Only one remote data configuration is allowed."));
    }

}