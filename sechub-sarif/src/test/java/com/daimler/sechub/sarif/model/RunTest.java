package com.daimler.sechub.sarif.model;


import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.PojoTester;

class RunTest {

    @Test
    void values_are_null() {
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
    void values_are_not_null() {
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
    void test_setters() {
        /* prepare */
        Run run = new Run();

        /* execute +test */
        PojoTester.testSetterAndGetter(run);
    }

}
