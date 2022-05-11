// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.test.TestFileReader;

class ReportingDescriptorTest {

    @Test
    void reporting_descriptor_json_serialization_works_correctly() throws IOException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        File referenceFile = new File("src/test/resources/examples/sarifpropertysnippets/reportingdescriptor.sarif.json");

        /* execute */
        String jsonReferenceFromFile = TestFileReader.loadTextFile(referenceFile);
        assertNotNull(jsonReferenceFromFile);
        // Use Rule.class here since we need a concrete type and ReportingDescriptor is
        // abstract
        ReportingDescriptor reportingDescriptor = mapper.reader().readValue(jsonReferenceFromFile, Rule.class);
        String jsonCreatedFromObject = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportingDescriptor);
        assertNotNull(jsonCreatedFromObject);

        /* test */
        assertEquals(jsonReferenceFromFile, jsonCreatedFromObject);
    }

}
