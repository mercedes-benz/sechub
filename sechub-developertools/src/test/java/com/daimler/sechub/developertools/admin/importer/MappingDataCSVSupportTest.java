package com.daimler.sechub.developertools.admin.importer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class MappingDataCSVSupportTest {
    
    private MappingDataCSVSupport supportToTest;

    @Before
    public void before() {
        supportToTest = new MappingDataCSVSupport();
    }

    @Test
    public void from_csv_test_no_headline() {
        /* prepare */
        MappingData expected = new MappingData();
        expected.getEntries().add(new MappingEntry("pattern1", "replacement1", "comment1"));
        expected.getEntries().add(new MappingEntry("pattern2", "replacement2", "comment2"));
        
        List<CSVRow> rows = new ArrayList<>();
        CSVRow row1 = new CSVRow();
        row1.add("pattern1");
        row1.add("replacement1");
        row1.add("comment1");
        
        CSVRow row2 = new CSVRow();
        row2.add("pattern2");
        row2.add("replacement2");
        row2.add("comment2");
        
        rows.add(row1);
        rows.add(row2);
        
        /* execute */
        MappingData result = supportToTest.fromCSVRows(rows,0);
        
        /* prepare */
        assertEquals(expected.toJSON(), result.toJSON());
    
    }
    
    @Test
    public void from_csv_test_1_headline() {
        /* prepare */
        MappingData expected = new MappingData();
        expected.getEntries().add(new MappingEntry("pattern1", "replacement1", "comment1"));
        expected.getEntries().add(new MappingEntry("pattern2", "replacement2", "comment2"));
        
        List<CSVRow> rows = new ArrayList<>();
        CSVRow headline = new CSVRow();
        headline.add("patterns");
        headline.add("replacements");
        headline.add("comments");
        
        CSVRow row1 = new CSVRow();
        row1.add("pattern1");
        row1.add("replacement1");
        row1.add("comment1");
        
        CSVRow row2 = new CSVRow();
        row2.add("pattern2");
        row2.add("replacement2");
        row2.add("comment2");
        
        rows.add(row1);
        rows.add(row2);
        
        /* execute */
        MappingData result = supportToTest.fromCSVRows(rows,0);
        
        /* prepare */
        assertEquals(expected.toJSON(), result.toJSON());
    
    }
    
    
    @Test
    public void to_csv_test() {
        MappingData data = new MappingData();
        data.getEntries().add(new MappingEntry("pattern1", "replacement1", "comment1"));
        data.getEntries().add(new MappingEntry("pattern2", "replacement2", "comment2"));
        
        /* execute */
        List<CSVRow> result = supportToTest.toCSVRows(data);
        
        /* test */
        assertEquals(3, result.size()); // always with headlines
        int row=0;
        CSVRow headline = result.get(row++);
        CSVRow row1 = result.get(row++);
        CSVRow row2 = result.get(row++);
        
        int col=0;
        assertEquals(3,headline.columns.size());
        assertEquals(3,row1.columns.size());
        assertEquals(3,row2.columns.size());
        assertEquals("Pattern", headline.columns.get(col).cell);
        assertEquals("pattern1", row1.columns.get(col).cell);
        assertEquals("pattern2", row2.columns.get(col++).cell);
        assertEquals("Replacement", headline.columns.get(col).cell);
        assertEquals("replacement1", row1.columns.get(col).cell);
        assertEquals("replacement2", row2.columns.get(col++).cell);
        assertEquals("Comment", headline.columns.get(col).cell);
        assertEquals("comment1", row1.columns.get(col).cell);
        assertEquals("comment2", row2.columns.get(col).cell);
        
    }

}
