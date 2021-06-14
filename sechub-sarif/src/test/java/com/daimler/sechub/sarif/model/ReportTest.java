package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ReportTest {

    @Test
    public void value_is_null() {
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
    public void value_is_not_null() {
        /* prepare */
        Report report = new Report(SarifVersion.VERSION_210);

        /* execute */
        String version = report.getVersion();
        String schema = report.get$schema();
        List<Run> runs = report.getRuns();

        /* test */
        assertEquals(version, "2.1.0");
        assertEquals(schema,
                "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json");

        assertTrue(runs.isEmpty());
    }
}
