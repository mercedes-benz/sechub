// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.adapter.SecHubTimeUnit;
import com.daimler.sechub.adapter.SecHubTimeUnitData;

public class SecHubTimeUnitDataTest {
    @Test
    public void construction() {
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.DAY);
        
        /* test */
        assertNotNull(timeUnitData);
        assertEquals(1, timeUnitData.getTime());
        assertEquals(SecHubTimeUnit.DAY, timeUnitData.getUnit());
    }
    
    @Test
    public void construction_zero() {
        /* prepare */
        String expectedMessage = "A time value of zero or a negative value is not accepted";

        /* execute + test */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SecHubTimeUnitData.of(0, SecHubTimeUnit.DAY);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
    }
    
    @Test
    public void construction_negative() {
        /* prepare */
        String expectedMessage = "A time value of zero or a negative value is not accepted";

        /* execute + test */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SecHubTimeUnitData.of(-12930, SecHubTimeUnit.DAY);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
    }
    
    @Test
    public void get_milliseconds_one_millisecond() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.MILLISECOND);
        
        /* test */
        assertEquals(1, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_one_second() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.SECOND);
        
        /* test */
        assertEquals(1000, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_one_minute() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.MINUTE);
        
        /* test */
        assertEquals(1000*60, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_three_minutes() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(3, SecHubTimeUnit.MINUTE);
        
        /* test */
        assertEquals(1000*60*3, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_one_hour() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.HOUR);
        
        /* test */
        assertEquals(1000*60*60, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_27_hours() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(27, SecHubTimeUnit.HOUR);
        
        /* test */
        assertEquals(1000*60*60*27, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_one_day() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.DAY);
        
        /* test */
        assertEquals(1000*60*60*24, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_milliseconds_seven_days() {        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(7, SecHubTimeUnit.DAY);
        
        /* test */
        assertEquals(1000*60*60*24*7, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void milliseconds_max() {
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(Integer.MAX_VALUE, SecHubTimeUnit.MILLISECOND);
        
        /* test */
        assertEquals(Integer.MAX_VALUE, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void milliseconds_max_plus_one() {
        /* prepare */
        String expectedMessage = "A time value of zero or a negative value is not accepted";

        /* execute + test */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SecHubTimeUnitData.of(Integer.MAX_VALUE + 1, SecHubTimeUnit.MILLISECOND);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
    }
    
    @Test
    public void days_max() {
        /* prepare */
        long maxDaysInMilliseconds = SecHubTimeUnit.DAY.getMultiplicatorMilliseconds() * Integer.MAX_VALUE;
       
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(Integer.MAX_VALUE, SecHubTimeUnit.DAY);
        
        /* test */
        assertEquals(maxDaysInMilliseconds, timeUnitData.getTimeInMilliseconds());
    }
    
    @Test
    public void get_time_in_hours_one_hour() {
        /* prepare */
        long hours = 1;
        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.HOUR);
        
        /* test */
        assertEquals(hours, timeUnitData.getTimeInHours());
    }
    
    @Test
    public void get_time_in_hours_61_minutes() {
        /* prepare */
        long hours = 2;
        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(61, SecHubTimeUnit.MINUTE);
        
        /* test */
        assertEquals(hours, timeUnitData.getTimeInHours());
    }
    
    @Test
    public void get_time_in_hours_60_minutes() {
        /* prepare */
        long hours = 1;
        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(60, SecHubTimeUnit.MINUTE);
        
        /* test */
        assertEquals(hours, timeUnitData.getTimeInHours());
    }
    
    @Test
    public void get_time_in_hours_59_minutes() {
        /* prepare */
        long hours = 1;
        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(59, SecHubTimeUnit.MINUTE);
        
        /* test */
        assertEquals(hours, timeUnitData.getTimeInHours());
    }
    
    @Test
    public void get_time_in_hours_1_millisecond() {
        /* prepare */
        long hours = 1;
        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(1, SecHubTimeUnit.MILLISECOND);
        
        /* test */
        assertEquals(hours, timeUnitData.getTimeInHours());
    }
    
    @Test
    public void get_time_in_hours_2_days() {
        /* prepare */
        long hours = 48;
        
        /* execute */
        SecHubTimeUnitData timeUnitData = SecHubTimeUnitData.of(2, SecHubTimeUnit.DAY);
        
        /* test */
        assertEquals(hours, timeUnitData.getTimeInHours());
    }
}
