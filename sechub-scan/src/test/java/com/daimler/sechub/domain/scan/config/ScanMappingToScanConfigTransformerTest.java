// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class ScanMappingToScanConfigTransformerTest {

    private ScanMappingToScanConfigTransformer transformerToTest;
    private MappingDataToNamePatternToIdEntryConverter converter;

    @Before
    public void before() {
        transformerToTest = new ScanMappingToScanConfigTransformer();

        /* mock converter */
        converter = mock(MappingDataToNamePatternToIdEntryConverter.class);
        transformerToTest.converter = converter;
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
        MappingData data = createMappingData(new MappingEntry("x", "y", "z"));
        ScanMapping mapping1 = createMapping("id1", data);
        when(converter.convert(any())).thenReturn(Arrays.asList(new NamePatternToIdEntry("pattern1", "replacement1")));

        /* execute */
        ScanConfig result = transformerToTest.transform(Collections.singletonList(mapping1));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.getNamePatternMappings().size());
        List<NamePatternToIdEntry> namePatternList = result.getNamePatternMappings().get("id1");
        assertEquals(1, namePatternList.size());

        NamePatternToIdEntry first = namePatternList.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());
    }

    @Test
    public void one_mapping_containing_two_entries_transformed_to_config() {
        /* prepare */
        MappingData data = createMappingData(new MappingEntry("x", "y", "z"),
                new MappingEntry("a", "b", "c"));
        ScanMapping mapping1 = createMapping("id1", data);
        when(converter.convert(any()))
                .thenReturn(Arrays.asList(new NamePatternToIdEntry("pattern1", "replacement1"), new NamePatternToIdEntry("pattern2", "replacement2")));

        /* execute */
        ScanConfig result = transformerToTest.transform(Collections.singletonList(mapping1));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.getNamePatternMappings().size());
        List<NamePatternToIdEntry> namePatternList = result.getNamePatternMappings().get("id1");
        assertEquals(2, namePatternList.size());

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
        MappingData data1 = createMappingData(new MappingEntry("x", "y", "z"),
                new MappingEntry("a", "b", "c"));
        ScanMapping mapping1 = createMapping("id1", data1);
        MappingData data2 = createMappingData(new MappingEntry("d", "e", null), new MappingEntry("f", "g", null));
        ScanMapping mapping2 = createMapping("id2", data2);

        when(converter.convert(any()))
                .thenReturn(Arrays.asList(new NamePatternToIdEntry("pattern1", "replacement1"), new NamePatternToIdEntry("pattern2", "replacement2")))
                .thenReturn(Arrays.asList(new NamePatternToIdEntry("pattern3", "replacement3"), new NamePatternToIdEntry("pattern4", "replacement4")));

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
        assertEquals(2, namePatternList1.size());

        NamePatternToIdEntry first = namePatternList1.get(0);
        assertEquals("pattern1", first.getNamePattern());
        assertEquals("replacement1", first.getId());

        NamePatternToIdEntry second = namePatternList1.get(1);
        assertEquals("pattern2", second.getNamePattern());
        assertEquals("replacement2", second.getId());

        /* -- id:2-- */
        List<NamePatternToIdEntry> namePatternList2 = result.getNamePatternMappings().get("id2");
        assertEquals(2, namePatternList2.size());

        NamePatternToIdEntry first2 = namePatternList2.get(0);
        assertEquals("pattern3", first2.getNamePattern());
        assertEquals("replacement3", first2.getId());

        NamePatternToIdEntry second2 = namePatternList2.get(1);
        assertEquals("pattern4", second2.getNamePattern());
        assertEquals("replacement4", second2.getId());
    }

    private ScanMapping createMapping(String id, MappingData data) {
        ScanMapping mapping = new ScanMapping(id);

        mapping.setData(data.toJSON());

        return mapping;
    }

    private MappingData createMappingData(MappingEntry... entries) {
        MappingData data = new MappingData();
        for (MappingEntry entry : entries) {
            data.getEntries().add(entry);
        }
        return data;
    }

}
