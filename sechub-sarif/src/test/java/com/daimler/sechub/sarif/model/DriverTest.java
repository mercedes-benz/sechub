// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.PojoTester;

class DriverTest {

    private Driver driverToTest;

    @BeforeEach
    void beforeEach() {
        driverToTest = new Driver();
    }

    @Test
    void check_initial_values() {
        /* execute */
        String name = driverToTest.getName();
        String informationUri = driverToTest.getInformationUri();
        String version = driverToTest.getVersion();
        List<Rule> rules = driverToTest.getRules();

        /* test */
        assertEquals(name, null);
        assertEquals(informationUri, null);
        assertEquals(version, null);
        assertNotNull(rules);
    }

    @Test
    void test_setters() {

        PojoTester.testSetterAndGetter(driverToTest);
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode( createExample(), createExample());
        
        testBothAreNOTEqual( createExample(), change(createExample(), (driver) -> driver.setInformationUri("other") ));
        testBothAreNOTEqual( createExample(), change(createExample(), (driver) -> driver.setName("other") ));
        testBothAreNOTEqual( createExample(), change(createExample(), (driver) -> driver.setRules(Collections.singletonList(new Rule())) ));
        testBothAreNOTEqual( createExample(), change(createExample(), (driver) -> driver.setVersion("1.10") ));
        /* @formatter:on */

    }

    private Driver createExample() {
        return initDriverwithFixExampleData(new Driver());
    }

    private Driver initDriverwithFixExampleData(Driver driver) {
        driver.setName("tool-name");
        driver.setVersion("v1.9");
        driver.setInformationUri("https://www.example.com/toool/documentation");
        driver.setRules(new ArrayList<>());
        return driver;
    }

}
