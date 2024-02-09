// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class SecHubConfigurationModelTest {

    @Test
    void deserialization_of_a_configuration_with_metadata_having_labels_results_in_configuration_with_those_metadata_labels() {
        /* @formatter:off */

        /* prepare */
        String json ="{\n"
                + "  \"apiVersion\" : \"1.0\",\n"
                + "  \"metaData\": {\n"
                + "     \"labels\": {\n"
                + "        \"stage\": \"testing\", \n"
                + "        \"purpose\": \"quality assurance\" \n"
                + "    }\n"
                + "  },\n"
                + "  \"data\" : {\n"
                + "    \"binaries\" : [ {\n"
                + "      \"name\" : \"myproject\",\n"
                + "      \"fileSystem\" : {\n"
                + "        \"folders\" : [ \"myproject/code\" ]\n"
                + "      }\n"
                + "    } ]\n"
                + "  },\n"
                + "  \"codeScan\" : {\n"
                + "    \"use\" : [\"myproject\"]\n"
                + "  }\n"
                + "}";

        /* execute */
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);


        /* test */
        Optional<SecHubConfigurationMetaData> metaDataOpt = model.getMetaData();
        assertTrue(metaDataOpt.isPresent());

        Map<String, String> labels = metaDataOpt.get().getLabels();
        assertEquals(2,labels.keySet().size());
        assertEquals("testing", labels.get("stage"));
        assertEquals("quality assurance", labels.get("purpose"));


        /* @formatter:on */

    }

    @Test
    void deserialization_of_a_configuration_without_metadata_has_no_metadata() {
        /* @formatter:off */

        /* prepare */
        String json ="{\n"
                + "  \"apiVersion\" : \"1.0\",\n"
                + "  \"data\" : {\n"
                + "    \"binaries\" : [ {\n"
                + "      \"name\" : \"myproject\",\n"
                + "      \"fileSystem\" : {\n"
                + "        \"folders\" : [ \"myproject/code\" ]\n"
                + "      }\n"
                + "    } ]\n"
                + "  },\n"
                + "  \"codeScan\" : {\n"
                + "    \"use\" : [\"myproject\"]\n"
                + "  }\n"
                + "}";

        /* execute */
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);


        /* test */
        Optional<SecHubConfigurationMetaData> metaDataOpt = model.getMetaData();
        assertFalse(metaDataOpt.isPresent());

        /* @formatter:on */

    }

    @Test
    void deserialization_of_a_configuration_with_excludes_includes_contains_them() {

        /* prepare */
        String json = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "binaries" : [ {
                      "name" : "bin-reference-example2",
                      "fileSystem" : {
                        "folders" : [ "bin-ref2-folder1" ],
                        "files" : [ "bin-file-reference.txt" ]
                      },
                       "excludes": [
                          "**/filtered-folder/**",
                          "must-not-be-contained.*"
                        ],
                        "includes": [
                          "something-important.txt"
                        ]
                    }, {
                      "name" : "bin-reference-example3",
                      "fileSystem" : {
                        "folders" : [ "bin-ref3-folder1" ],
                        "files" : [ "bin-file-reference.txt" ]
                      }
                    } ],
                    "sources" : [ {
                      "name" : "source-reference-example1",
                      "fileSystem" : {
                        "folders" : [ "source-ref1-folder" ],
                        "files" : [ "src-file-reference1.txt", "src-file-reference2.txt" ]
                      },
                      "excludes": [
                          "**/filtered-folder/**",
                          "must-not-be-contained.*"
                        ]
                    } ]
                  },
                  "codeScan" : {
                    "use" : [ "source-reference-example" ],
                    "fileSystem" : {
                      "folders" : [ "legacy", "legacy2" ],
                      "files" : [ "legacy-file3.txt" ]
                    },
                    "excludes" : [ "**/filtered-folder/**", "must-not-be-contained.*" ],
                    "licenseScan" : {
                      "use" : [ "bin-ref2" ]
                    }
                  }
                }
                """;

        /* execute */
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        /* test */
        SecHubCodeScanConfiguration codeScan = model.getCodeScan().get();
        assertTrue(codeScan.getExcludes().contains("**/filtered-folder/**"));
        assertTrue(codeScan.getExcludes().contains("must-not-be-contained.*"));

        SecHubDataConfiguration data = model.getData().get();

        SecHubBinaryDataConfiguration binReferenceExample2 = data.getBinaries().iterator().next();
        assertEquals("bin-reference-example2", binReferenceExample2.getUniqueName());
        assertTrue(binReferenceExample2.getExcludes().contains("**/filtered-folder/**"));
        assertTrue(binReferenceExample2.getExcludes().contains("must-not-be-contained.*"));
        assertTrue(binReferenceExample2.getIncludes().contains("something-important.txt"));

    }

    @Test
    void serialize_without_config() {
        /* prepare */
        SecHubConfigurationModel configuration = new SecHubConfigurationModel();
        configuration.setApiVersion("1.0");

        /* execute */
        String json = JSONConverter.get().toJSON(configuration);

        /* test */
        assertNotNull(json);
    }

    @Test
    void serialize_without_codeScan_config() {
        /* prepare */
        SecHubConfigurationModel configuration = new SecHubConfigurationModel();
        configuration.setApiVersion("1.0");

        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        configuration.setCodeScan(codeScan);

        /* execute */
        String json = JSONConverter.get().toJSON(configuration);

        /* test */
        assertNotNull(json);
    }

    @Test
    void deserialize_codeScan_config_and_empty_use() {
        /* prepare */
        String json = """
                {
                  "codeScan" : {
                    "use" : [ ]
                  },
                  "apiVersion" : "1.0"
                }
        """;

        /* execute */
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        /* test */
        SecHubCodeScanConfiguration codeScan = model.getCodeScan().get();
        assertNotNull(codeScan);
    }
}
