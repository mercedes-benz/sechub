// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SerecoSourceRelevantPartResolverTest {

    private SerecoSourceRelevantPartResolver resolverToTest;

    @BeforeEach
    void beforeEach() throws Exception {
        resolverToTest = new SerecoSourceRelevantPartResolver();
    }

    @Test
    void shrink_whitespaces() {
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will be shrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will   be    shrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will beshrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will\t\tbeshrinked"));
        assertEquals("iwillbeshrinked", resolverToTest.toRelevantPart("i will\t\nbeshrinked\n  "));
    }

    @Test
    void lowercased() {
        assertEquals("lowered", resolverToTest.toRelevantPart("loWEREd"));
        assertEquals("lowered", resolverToTest.toRelevantPart("LOWERED"));
        assertEquals("lowered", resolverToTest.toRelevantPart("lowered"));
    }

    @Test
    void null_source_returns_empty_string() {
        assertEquals("", resolverToTest.toRelevantPart(null));
    }

    @Test
    void empty_source_returns_empty_string() {
        assertEquals("", resolverToTest.toRelevantPart(""));
    }

}
