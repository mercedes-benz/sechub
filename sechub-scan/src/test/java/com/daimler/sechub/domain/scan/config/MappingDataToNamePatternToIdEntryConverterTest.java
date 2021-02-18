// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class MappingDataToNamePatternToIdEntryConverterTest {

    private MappingDataToNamePatternToIdEntryConverter converterToTest;

    @Before
    public void before() {
        converterToTest = new MappingDataToNamePatternToIdEntryConverter();
    }

    @Test
    public void null_transformed_to_config() {
        /* execute */
        List<NamePatternToIdEntry> result = converterToTest.convert(null);

        /* test */
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void one_mapping_containing_one_entry_transformed_to_config() {
        /* prepare */
        MappingData mapping1 = createMapping(new MappingEntry("pattern1","replacement1","comment1"));
        
        /* execute */
        List<NamePatternToIdEntry> namePatternList = converterToTest.convert(mapping1);
        
        /* test */
        assertNotNull(namePatternList);
        assertEquals(1, namePatternList.size());
        assertEquals(1,namePatternList.size());
        
        NamePatternToIdEntry first = namePatternList.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());
    }
    
    @Test
    public void one_mapping_containing_two_entries_transformed_to_config() {
        /* prepare */
        MappingData mapping1 = createMapping(new MappingEntry("pattern1","replacement1","comment1"),new MappingEntry("pattern2","replacement2","comment2"));
        
        /* execute */
        List<NamePatternToIdEntry> namePatternList = converterToTest.convert(mapping1);
        
        /* test */
        assertNotNull(namePatternList);
        assertEquals(2,namePatternList.size());
        
        NamePatternToIdEntry first = namePatternList.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());
        
        NamePatternToIdEntry second = namePatternList.get(1);
        assertEquals("pattern2", second.getNamePattern());
        assertEquals("replacement2", second.getId());
    }
    
    private MappingData createMapping(MappingEntry ...entries) {
        MappingData data = new MappingData();
       
        for (MappingEntry entry: entries) {
            data.getEntries().add(entry);
        }
        return data;
    }

}
