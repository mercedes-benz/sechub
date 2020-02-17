// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class ScanMappingToScanConfigTransformerTest {

    private ScanMappingToScanConfigTransformer transformerToTest;

    @Before
    public void before() {
        transformerToTest = new ScanMappingToScanConfigTransformer();
    }

    @Test
    public void null_transformed_to_config() {
        /* execute */
        ScanConfig result = transformerToTest.transform(null);

        /* test */
        assertNotNull(result);
        assertEquals(0, result.getNamePatternMappings().size());
    }
    
    @Test
    public void empty_list_transformed_to_config() {
        /* execute */
        ScanConfig result = transformerToTest.transform(new ArrayList<>());

        /* test */
        assertNotNull(result);
        assertEquals(0, result.getNamePatternMappings().size());
    }
    
    @Test
    public void one_mapping_containing_one_entry_transformed_to_config() {
        /* prepare */
        ScanMapping mapping1 = createMapping("id1",new MappingEntry("pattern1","replacement1","comment1"));
        
        /* execute */
        ScanConfig result = transformerToTest.transform(Collections.singletonList(mapping1));
        
        /* test */
        assertNotNull(result);
        assertEquals(1, result.getNamePatternMappings().size());
        List<NamePatternToIdEntry> namePatternList = result.getNamePatternMappings().get("id1");
        assertEquals(1,namePatternList.size());
        
        NamePatternToIdEntry first = namePatternList.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());
    }
    
    @Test
    public void one_mapping_containing_two_entries_transformed_to_config() {
        /* prepare */
        ScanMapping mapping1 = createMapping("id1",new MappingEntry("pattern1","replacement1","comment1"),new MappingEntry("pattern2","replacement2","comment2"));
        
        /* execute */
        ScanConfig result = transformerToTest.transform(Collections.singletonList(mapping1));
        
        /* test */
        assertNotNull(result);
        assertEquals(1, result.getNamePatternMappings().size());
        List<NamePatternToIdEntry> namePatternList = result.getNamePatternMappings().get("id1");
        assertEquals(2,namePatternList.size());
        
        NamePatternToIdEntry first = namePatternList.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());
        
        NamePatternToIdEntry second = namePatternList.get(1);
        assertEquals("pattern2", second.getNamePattern());
        assertEquals("replacement2", second.getId());
    }
    
    @Test
    public void two_mappings_containing_four_entries_transformed_to_config() {
        /* prepare */
        ScanMapping mapping1 = createMapping("id1",new MappingEntry("pattern1","replacement1","comment1"),new MappingEntry("pattern2","replacement2","comment2"));
        ScanMapping mapping2 = createMapping("id2",new MappingEntry("pattern3","replacement3",null),new MappingEntry("pattern4","replacement4",null));
        
        List<ScanMapping> list = new ArrayList<>();
        list.add(mapping1);
        list.add(mapping2);
        
        /* execute */
        ScanConfig result = transformerToTest.transform(list);
        
        /* test */
        assertNotNull(result);
        assertEquals(2, result.getNamePatternMappings().size());
        
        /* -- id:1-- */
        List<NamePatternToIdEntry> namePatternList1 = result.getNamePatternMappings().get("id1");
        assertEquals(2,namePatternList1.size());
        
        NamePatternToIdEntry first = namePatternList1.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());
        
        NamePatternToIdEntry second = namePatternList1.get(1);
        assertEquals("pattern2", second.getNamePattern());
        assertEquals("replacement2", second.getId());
        
        /* -- id:2-- */
        List<NamePatternToIdEntry> namePatternList2 = result.getNamePatternMappings().get("id2");
        assertEquals(2,namePatternList2.size());
        
        NamePatternToIdEntry first2 = namePatternList2.get(0);
        assertEquals("pattern3", first2.getNamePattern());
        assertEquals("replacement3", first2.getId());
        
        NamePatternToIdEntry second2 = namePatternList2.get(1);
        assertEquals("pattern4", second2.getNamePattern());
        assertEquals("replacement4", second2.getId());
    }
    
    
    
    private ScanMapping createMapping(String id, MappingEntry ...entries) {
        ScanMapping mapping = new ScanMapping(id);
       
        MappingData data = new MappingData();
        for (MappingEntry entry: entries) {
            data.getEntries().add(entry);
        }
        mapping.setData(data.toJSON());
        
        return mapping;
    }

}
