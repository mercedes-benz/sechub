// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReportTest {

    @Test
    public void constructor_null_param() {
        /* prepare */
        Report report = new Report(null);

        /* execute */
        String version = report.getVersion();
        String schema = report.get$schema();
        List<Run> runs = report.getRuns();

        /* test */
        assertEquals(version, null);
        assertEquals(schema, null);

        assertTrue(runs.isEmpty());
    }

    @Test
    void constructor_param_version_set() {
        /* prepare */
        Report report = new Report(SarifVersion.VERSION_2_1_0);

        /* execute */
        String version = report.getVersion();
        String schema = report.get$schema();
        List<Run> runs = report.getRuns();

        /* test */
        assertEquals(version, "2.1.0");
        assertEquals(schema, "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json");

        assertTrue(runs.isEmpty());
    }

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());
        testBothAreNOTEqual(createExample(), change(createExample(), (report) -> report.set$schema("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (report) -> report.setVersion("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (report) -> report.setRuns(Collections.singletonList(new Run()))));
        /* @formatter:on */

    }

    private Report createExample() {
        return new Report();
    }
}
