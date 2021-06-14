package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegionTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_negative_startLine() {
        /* test */
        @SuppressWarnings("unused")
        Region region = new Region(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_negative_startColumn() {
        /* test */
        @SuppressWarnings("unused")
        Region region = new Region(0, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void set_negative_startLine() {
        /* prepare */
        Region region = new Region();
        
        /* test */
        region.setStartLine(-4);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void set_negative_startColumn() {
        /* prepare */
        Region region = new Region();
        
        /* test */
        region.setStartColumn(-5);
    }

    @Test
    public void test_zero() {
        /* prepare */
        Region region = new Region(0, 0);

        /* execute */
        long startLine = region.getStartLine();
        long startColumn = region.getStartColumn();

        /* test */
        assertEquals(startLine, 0);
        assertEquals(startColumn, 0);
    }

    @Test
    public void test_positive_values() {
        /* prepare */
        Region region = new Region(103, 17);

        /* execute */
        long startLine = region.getStartLine();
        long startColumn = region.getStartColumn();

        /* test */
        assertEquals(startLine, 103);
        assertEquals(startColumn, 17);
    }

    @Test
    public void test_get_and_set() {
        /* prepare */
        Region region = new Region();

        /* execute */
        region.setStartLine(23);
        region.setStartColumn(51);

        /* test */
        assertEquals(region.getStartLine(), 23);
        assertEquals(region.getStartColumn(), 51);
    }

}
