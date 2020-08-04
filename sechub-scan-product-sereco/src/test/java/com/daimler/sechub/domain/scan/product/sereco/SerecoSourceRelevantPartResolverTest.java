package com.daimler.sechub.domain.scan.product.sereco;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SerecoSourceRelevantPartResolverTest {

    private SerecoSourceRelevantPartResolver resolverToTest;

    @Before
    public void before() throws Exception {
        resolverToTest = new SerecoSourceRelevantPartResolver();
    }

    @Test
    public void shrink_whitespaces() {
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will be shrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will   be    shrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will beshrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will\t\tbeshrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will\t\nbeshrinked\n  "));
    }
    
    @Test
    public void lowercased() {
        assertEquals("lowered", resolverToTest.toRelevantPart("loWEREd"));
        assertEquals("lowered", resolverToTest.toRelevantPart("LOWERED"));
        assertEquals("lowered", resolverToTest.toRelevantPart("lowered"));
    }

}
