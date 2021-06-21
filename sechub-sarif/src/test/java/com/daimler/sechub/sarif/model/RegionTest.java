// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegionTest {

    @Test
    void test_negative_startLine() {
        /* test */
        assertThrows(IllegalArgumentException.class, () -> {
            new Region(-1, 0);
        });
    }

    @Test
    void test_negative_startColumn() {
        /* test */
        assertThrows(IllegalArgumentException.class, () -> {
            new Region(0, -1);
        });
    }

    @Test
    void set_negative_startLine() {
        /* prepare */
        Region region = new Region();

        /* test */
        assertThrows(IllegalArgumentException.class, () -> {
            region.setStartLine(-4);
        });
    }

    @Test
    void set_negative_startColumn() {
        /* prepare */
        Region region = new Region();

        /* test */
        assertThrows(IllegalArgumentException.class, () -> {
            region.setStartColumn(-5);
        });
    }

    @Test
    void test_zero() {
        /* prepare */
        Region region = new Region(0, 0);

        /* execute */
        long startLine = region.getStartLine();
        long startColumn = region.getStartColumn();

        /* test */
        assertEquals(startLine, 0);
        assertEquals(startColumn, 0);
    }

    @Test
    void test_positive_values() {
        /* prepare */
        Region region = new Region(103, 17);

        /* execute */
        long startLine = region.getStartLine();
        long startColumn = region.getStartColumn();

        /* test */
        assertEquals(startLine, 103);
        assertEquals(startColumn, 17);
    }

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (region) -> region.setStartLine(42)));
        testBothAreNOTEqual(createExample(), change(createExample(), (region) -> region.setStartColumn(42)));
        /* @formatter:on */

    }

    private Region createExample() {
        return new Region();
    }

}
