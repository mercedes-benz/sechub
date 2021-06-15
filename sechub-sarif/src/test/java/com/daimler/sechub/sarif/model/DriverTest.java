package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.daimler.sechub.test.PojoTester;

class DriverTest {

    @Test
    void values_are_null() {
        /* prepare */
        Driver driver = new Driver(null, null, null, null);

        /* execute */
        String name = driver.getName();
        String informationUri = driver.getInformationUri();
        String version = driver.getVersion();
        List<Rule> rules = driver.getRules();

        /* test */
        assertEquals(name, null);
        assertEquals(informationUri, null);
        assertEquals(version, null);
        assertEquals(rules, null);
    }

    @Test
    void values_are_not_null() {
        /* prepare */
        Driver driver = new Driver("tool-name", "v1.9", "https://www.tool.org/documentation", new LinkedList<Rule>());

        /* execute */
        String name = driver.getName();
        String version = driver.getVersion();
        String informationUri = driver.getInformationUri();
        List<Rule> rules = driver.getRules();

        /* test */
        assertEquals(name, "tool-name");
        assertEquals(version, "v1.9");
        assertEquals(informationUri, "https://www.tool.org/documentation");

        assertTrue(rules.isEmpty());
    }

    @Test
    void test_setters() {
        /* prepare */
        Driver driver = new Driver();


        /* execute + test */
        PojoTester.testSetterAndGetter(driver);
    }

}
