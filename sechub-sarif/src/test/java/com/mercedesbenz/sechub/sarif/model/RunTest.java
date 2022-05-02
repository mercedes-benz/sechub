// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RunTest {

    @Test
    void constructor_params_null() {
        /* prepare */
        Run run = new Run(null, null, null, null);

        /* execute */
        Tool tool = run.getTool();
        List<Result> results = run.getResults();
        List<Taxonomy> taxonomies = run.getTaxonomies();
        List<VersionControlDetails> versionControlProvenance = run.getVersionControlProvenance();

        /* test */
        assertEquals(tool, null);
        assertEquals(results, null);
        assertEquals(taxonomies, null);
        assertEquals(versionControlProvenance, null);
    }

    @Test
    void constructor_params_not_null() {
        /* prepare */
        Run run = new Run(new Tool(), new LinkedList<Result>(), new LinkedList<Taxonomy>(), new LinkedList<VersionControlDetails>());

        /* execute */
        Tool tool = run.getTool();
        List<Result> results = run.getResults();
        List<Taxonomy> taxonomies = run.getTaxonomies();
        List<VersionControlDetails> versionControlProvenance = run.getVersionControlProvenance();

        /* test */
        assertEquals(tool, new Tool());
        assertEquals(results, new LinkedList<Result>());
        assertEquals(taxonomies, new LinkedList<Taxonomy>());
        assertEquals(versionControlProvenance, new LinkedList<VersionControlDetails>());
    }

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (run) -> run.setTool(new Tool())));
        /* @formatter:on */

    }

    private Run createExample() {
        Run run = new Run();
        return run;
    }
}
