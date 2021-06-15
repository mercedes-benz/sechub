package com.daimler.sechub.sarif.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.PojoTester;

class ToolTest {

    @Test
    void value_is_null() {
        /* prepare */
        Tool tool = new Tool(null);

        /* execute */
        Driver driver = tool.getDriver();

        /* test */
        assertEquals(driver, null);
    }

    @Test
    void value_is_not_null() {
        /* prepare */
        Tool tool = new Tool(new Driver());

        /* execute */
        Driver driver = tool.getDriver();

        /* test */
        assertEquals(driver, new Driver());
    }

    @Test
    void test_setter() {
        /* prepare */
        Tool tool = new Tool();

        /* execute */
        PojoTester.testSetterAndGetter(tool);
    }

}
