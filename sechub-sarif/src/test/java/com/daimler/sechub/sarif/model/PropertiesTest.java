package com.daimler.sechub.sarif.model;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.PojoTester;

class PropertiesTest {

    @Test
    void test_setter() {
        /* prepare */
        Properties properties = new Properties();

        /* execute + test */
        PojoTester.testSetterAndGetter(properties);
    }

}
