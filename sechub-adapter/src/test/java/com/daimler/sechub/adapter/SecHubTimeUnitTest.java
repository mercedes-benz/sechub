package com.daimler.sechub.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public class SecHubTimeUnitTest {   
    @Test
    public void get_multiplicator_milliseconds__get_millisecond() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.MILLISECOND;
        
        /* test */
        assertNotNull(unit);
        assertEquals(1, unit.getMultiplicatorMilliseconds());
    }
    
    @Test
    public void value_of__millisecond() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOf("MILLISECOND");
        
        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }
    
    @Test
    public void value_of__null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> {
            SecHubTimeUnit.valueOf(null);
        });
    }
    
    @Test
    public void from_json_millisecond() throws JsonMappingException, JsonProcessingException {
        /* prepare */
        String json = "\"millisecond\"";
        ObjectMapper objectMapper = new ObjectMapper();
        
        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);  
        
        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }
    
    @Test
    public void from_json_milliseconds() throws JsonMappingException, JsonProcessingException {
        /* prepare */
        String json = "\"milliseconds\"";
        ObjectMapper objectMapper = new ObjectMapper();
        
        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);  
        
        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }
    
    @Test
    public void from_json_hour() throws JsonMappingException, JsonProcessingException {
        /* prepare */
        String json = "\"HOUR\"";
        ObjectMapper objectMapper = new ObjectMapper();
        
        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);  
        
        /* test */
        assertEquals(SecHubTimeUnit.HOUR, unit);
    }
    
    @Test
    public void from_json_days() throws JsonMappingException, JsonProcessingException {
        /* prepare */
        String json = "\"days\"";
        ObjectMapper objectMapper = new ObjectMapper();
        
        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);  
        
        /* test */
        assertEquals(SecHubTimeUnit.DAY, unit);
    }
    
    @Test
    public void from_json_months() throws JsonMappingException, JsonProcessingException {
        /* prepare */
        String json = "\"months\"";
        ObjectMapper objectMapper = new ObjectMapper();
        
        /* execute + test */
        assertThrows(InvalidFormatException.class, () -> {
            objectMapper.readValue(json, SecHubTimeUnit.class); 
        });
    }
}
