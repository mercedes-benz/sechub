// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RunTest {

    @Test
    void all_attributes_null() {
        /* prepare */
        Run run = new Run();
        run.setTool(null);
        run.setResults(null);
        run.setTaxonomies(null);
        run.setVersionControlProvenance(null);

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
        Run run = new Run(new Tool(), new LinkedList<Result>());
        run.setTaxonomies(new LinkedList<Taxonomy>());
        run.setVersionControlProvenance(new LinkedList<VersionControlDetails>());

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
