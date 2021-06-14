package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ToolTest {

    @Test
    public void value_is_null() {
        /* prepare */
        Tool tool = new Tool(null);

        /* execute */
        Driver driver = tool.getDriver();

        /* test */
        assertEquals(driver, null);
    }

    @Test
    public void value_is_not_null() {
        /* prepare */
        Tool tool = new Tool(new Driver());

        /* execute */
        Driver driver = tool.getDriver();

        /* test */
        assertEquals(driver, new Driver());
    }

    @Test
    public void test_setter() {
        /* prepare */
        Tool tool = new Tool();

        /* execute */
        tool.setDriver(new Driver());

        /* test */
        assertEquals(tool.getDriver(), new Driver());
    }

}
