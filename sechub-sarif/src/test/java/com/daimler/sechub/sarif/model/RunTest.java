// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RunTest {

    @Test
    void constructor_params_null() {
        /* prepare */
        Run run = new Run(null, null);

        /* execute */
        Tool tool = run.getTool();
        List<Result> results = run.getResults();

        /* test */
        assertEquals(tool, null);
        assertEquals(results, null);
    }

    @Test
    void constructor_params_not_null() {
        /* prepare */
        Run run = new Run(new Tool(), new LinkedList<Result>());

        /* execute */
        Tool tool = run.getTool();
        List<Result> results = run.getResults();

        /* test */
        assertEquals(tool, new Tool());
        assertEquals(results, new LinkedList<Result>());
    }

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setTool(new Tool())));
        /* @formatter:on */

    }

    private Run createExample() {
        Run run = new Run();
        return run;
    }
}
