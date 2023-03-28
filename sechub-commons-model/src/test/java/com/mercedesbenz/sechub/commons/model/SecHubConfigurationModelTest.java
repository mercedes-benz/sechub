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

}
