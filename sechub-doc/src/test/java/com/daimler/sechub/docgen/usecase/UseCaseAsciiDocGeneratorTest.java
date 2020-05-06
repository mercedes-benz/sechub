package com.daimler.sechub.docgen.usecase;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UseCaseAsciiDocGeneratorTest {

    private UseCaseAsciiDocGenerator generatorToTest;

    @Before
    public void before() throws Exception {
        generatorToTest = new UseCaseAsciiDocGenerator();
    }
    
    @Test
    public void create_description_null_and_empty_return_empty_string() {
        assertEquals("", generatorToTest.createDescriptionForVariant(null));
        assertEquals("", generatorToTest.createDescriptionForVariant(""));
    }

    @Test
    public void create_description_simple_name_results_in_variant_description_and_simple_name() {
        assertEquals(" - variant: xxx", generatorToTest.createDescriptionForVariant("xxx"));
        assertEquals(" - variant: simple-name", generatorToTest.createDescriptionForVariant("simple-name"));
    }
    @Test
    public void create_description_replaces_underscores_with_spaces() {
        assertEquals(" - variant: x y z", generatorToTest.createDescriptionForVariant("x_y_z"));
    }

}
