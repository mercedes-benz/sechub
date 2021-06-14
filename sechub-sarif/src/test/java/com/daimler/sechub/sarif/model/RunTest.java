package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class RunTest {

    @Test
    public void values_are_null() {
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
    public void values_are_not_null() {
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
    public void test_setters() {
        /* prepare */
        Run run = new Run();

        /* execute */
        run.setTool(new Tool());
        run.setResults(new LinkedList<Result>());

        /* test */
        assertEquals(run.getTool(), new Tool());
        assertEquals(run.getResults(), new LinkedList<Result>());
    }

}
