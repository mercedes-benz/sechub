package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;

import org.junit.Test;

public class PropertiesTest {

    @Test
    public void test_setter() {
        /* prepare */
        Properties properties = new Properties();

        /* execute */
        properties.setTags(new LinkedHashSet<String>());

        /* test */
        assertTrue(properties.getTags().isEmpty());
    }

}
