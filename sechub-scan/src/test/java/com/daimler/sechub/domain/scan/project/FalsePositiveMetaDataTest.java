// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FalsePositiveMetaDataTest {

    private FalsePositiveMetaData metaData1;
    private FalsePositiveMetaData metaData2;

    @Before
    public void before() throws Exception {
        /* execute + test */
        metaData1 = new FalsePositiveMetaData();
        metaData2 = new FalsePositiveMetaData();
    }

    @Test
    public void nothing_set_equals() {
        /* execute + test */
        assertTrue(metaData1.equals(metaData2));
        assertTrue(metaData2.equals(metaData1));
    }
    
    @Test
    public void same_code_meta_data_is_equals() {
        /* prepare */
        FalsePositiveCodeMetaData code1 = new FalsePositiveCodeMetaData();
        FalsePositiveCodeMetaData code2 = new FalsePositiveCodeMetaData();
        
        FalsePositiveCodePartMetaData start1 = new FalsePositiveCodePartMetaData();
        start1.setLocation("l1");
        code1.setStart(start1);
        
        FalsePositiveCodePartMetaData start2 = new FalsePositiveCodePartMetaData();
        start2.setLocation("l1");
        code2.setStart(start2);
        
        metaData1.setCode(code1);
        metaData2.setCode(code2);
        
        /* execute + test */
        assertTrue(metaData1.equals(metaData2));
        assertTrue(metaData2.equals(metaData1));
    }
    
    @Test
    public void not_same_code_meta_data_is_not_equals() {
        /* prepare */
        FalsePositiveCodeMetaData code1 = new FalsePositiveCodeMetaData();
        FalsePositiveCodeMetaData code2 = new FalsePositiveCodeMetaData();
        
        FalsePositiveCodePartMetaData start1 = new FalsePositiveCodePartMetaData();
        start1.setLocation("l1");
        code1.setStart(start1);
        
        FalsePositiveCodePartMetaData start2 = new FalsePositiveCodePartMetaData();
        start2.setLocation("l2");
        code2.setStart(start2);
        
        metaData1.setCode(code1);
        metaData2.setCode(code2);
        
        /* execute + test */
        assertFalse(metaData1.equals(metaData2));
        assertFalse(metaData2.equals(metaData1));
    }
    
    

}
