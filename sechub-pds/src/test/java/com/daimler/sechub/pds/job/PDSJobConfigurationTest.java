package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfigurationTest {

    @Before
    public void before() throws Exception {

    }

    @Test
    public void fromJSON_json_containing_two_config_entries_like_in_concept_example_can_be_created() throws Exception{
        /* prepare */
        UUID sechubJobUUID = UUID.randomUUID();
        /* @formatter:off */
        String json ="{\n" + 
                "    \"apiVersion\" : \"1.0\",\n" + 
                "    \"sechubJobUUID\" : \""+sechubJobUUID.toString()+"\",\n" + 
                "\n" + 
                "    \"parameters\": [\n" + 
                "        {\n" + 
                "            \"key\" : \"sechub.test.key.1\", \n" + 
                "            \"value\" : \"value1\" \n" + 
                "        },\n" + 
                "        {\n" + 
                "            \"key\" : \"sechub.test.key.2\",\n" + 
                "            \"value\" : \"value2\"\n" + 
                "        }\n" + 
                "     ]\n" + 
                "}";
        /* @formatter:on */
        

        /* execute */
        PDSJobConfiguration result = PDSJobConfiguration.fromJSON(json);
        

        /* test */
        assertNotNull(result);
        assertEquals("1.0", result.getApiVersion());
        assertEquals(sechubJobUUID, result.getSechubJobUUID());

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

}
