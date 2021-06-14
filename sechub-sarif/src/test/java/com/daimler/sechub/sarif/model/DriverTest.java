package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class DriverTest {

    @Test
    public void values_are_null() {
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
    public void values_are_not_null() {
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
    public void test_setters() {
        /* prepare */
        Driver driver = new Driver();

        /* execute */
        driver.setName("tool-name");
        driver.setVersion("v1.9");
        driver.setInformationUri("https://www.tool.org/documentation");
        driver.setRules(new LinkedList<Rule>());

        /* test */
        assertEquals(driver.getName(), "tool-name");
        assertEquals(driver.getVersion(), "v1.9");
        assertEquals(driver.getInformationUri(), "https://www.tool.org/documentation");

        assertTrue(driver.getRules().isEmpty());
    }

}
