package com.daimler.sechub.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
    public void value_of_unit__millisecond() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("millisecond");
        
        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }
    
    @Test
    public void value_of_unit__milliseconds() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("milliseconds");
        
        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }
    
    @Test
    public void value_of_unit__millisecond_mixed_case() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("MilliSecond");
        
        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }
    
    @Test
    public void value_of_unit__second() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("second");
        
        /* test */
        assertEquals(SecHubTimeUnit.SECOND, unit);
    }
    
    @Test
    public void value_of_unit__seconds() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("seconds");
        
        /* test */
        assertEquals(SecHubTimeUnit.SECOND, unit);
    }
    
    @Test
    public void value_of_unit__minute() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("minute");
        
        /* test */
        assertEquals(SecHubTimeUnit.MINUTE, unit);
    }
    
    @Test
    public void value_of_unit__minutes() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("minutes");
        
        /* test */
        assertEquals(SecHubTimeUnit.MINUTE, unit);
    }
    
    @Test
    public void value_of_unit__hour() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("hour");
        
        /* test */
        assertEquals(SecHubTimeUnit.HOUR, unit);
    }
    
    @Test
    public void value_of_unit__hours() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("hours");
        
        /* test */
        assertEquals(SecHubTimeUnit.HOUR, unit);
    }
    
    @Test
    public void value_of_unit__day() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("day");
        
        /* test */
        assertEquals(SecHubTimeUnit.DAY, unit);
    }
    
    @Test
    public void value_of_unit__days() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOfUnit("days");
        
        /* test */
        assertEquals(SecHubTimeUnit.DAY, unit);
    }
    
    @Test
    public void value_of_unit__not_a_unit() {
        /* prepare */
        String expectedMessage = "A time unit of \"Orange\" is not an accepted time unit.";
        
        /* execute + test */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SecHubTimeUnit.valueOfUnit("Orange");
        });
        
        assertEquals(expectedMessage, exception.getMessage());
    }
    
    @Test
    public void value_of_unit__null() {
        /* prepare */
        String expectedMessage = "The time unit cannot be null.";
        
        /* execute + test */
        Exception exception = assertThrows(NullPointerException.class, () -> {
            SecHubTimeUnit.valueOfUnit(null);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
    }
}
