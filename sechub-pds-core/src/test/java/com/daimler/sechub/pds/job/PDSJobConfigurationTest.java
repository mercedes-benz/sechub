// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfigurationTest {

    @Before
    public void before() throws Exception {

    }

    @Test
    public void fromJSON_concept_example_file_can_be_loaded_and_contains_expected_data() throws Exception{
        /* prepare */
        File file = new File("./../sechub-doc/src/docs/asciidoc/documents/pds/product_delegation_job_config_example1.json");
        String json = FileUtils.readFileToString(file,"UTF-8");
        /* execute */
        PDSJobConfiguration result = PDSJobConfiguration.fromJSON(json);
        

        /* test */
        assertNotNull(result);
        assertEquals("1.0", result.getApiVersion());
        assertEquals(UUID.fromString("288607bf-ac81-4088-842c-005d5702a9e9"), result.getSechubJobUUID());

        List<PDSExecutionParameterEntry> config = result.getParameters();
        assertEquals(2, config.size());
        Iterator<PDSExecutionParameterEntry> ci = config.iterator();
        
        PDSExecutionParameterEntry entry1 = ci.next();
        assertEquals("sechub.test.key.1",entry1.getKey());
        assertEquals("value1",entry1.getValue());

        PDSExecutionParameterEntry entry2 = ci.next();
        assertEquals("sechub.test.key.2",entry2.getKey());
        assertEquals("value2",entry2.getValue());
    
    }
    
    
    @Test
    public void fromJSON_even_an_empty_json_can_be_transformed() throws Exception{
        /* prepare */
        String json="{}";

        /* execute */
        PDSJobConfiguration result = PDSJobConfiguration.fromJSON(json);
        
        /* test */
        assertNotNull(result);
        
        
    }

}
