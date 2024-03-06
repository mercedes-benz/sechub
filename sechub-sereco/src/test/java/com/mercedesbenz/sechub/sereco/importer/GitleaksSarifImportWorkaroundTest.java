// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.Tool;
import de.jcup.sarif_2_1_0.model.ToolComponent;

class GitleaksSarifImportWorkaroundTest {

    private GitleaksSarifImportWorkaround workaroundToTest = new GitleaksSarifImportWorkaround();

    @Test
    void rule_is_null_results_in_resolved_type_is_null() {
        /* execute */
        String resolvedType = workaroundToTest.resolveType(null, new Run());

        /* test */
        assertNull(resolvedType);
    }

    @Test
    void run_is_null_results_in_resolved_type_is_null() {
        /* execute */
        String resolvedType = workaroundToTest.resolveType(new ReportingDescriptor(), null);

        /* test */
        assertNull(resolvedType);
    }

    @Test
    void run_tool_is_null_results_in_resolved_type_is_null() {
        /* prepare */
        Run run = new Run();
        run.setTool(null);

        /* execute */
        String resolvedType = workaroundToTest.resolveType(new ReportingDescriptor(), run);

        /* test */
        assertNull(resolvedType);
    }

    @Test
    void run_tool_driver_is_null_results_in_resolved_type_is_null() {
        /* prepare */
        Run run = new Run();
        Tool tool = new Tool();
        tool.setDriver(null);
        run.setTool(tool);

        /* execute */
        String resolvedType = workaroundToTest.resolveType(new ReportingDescriptor(), run);

        /* test */
        assertNull(resolvedType);
    }

    @Test
    void run_tool_driver_name_is_gitleaks_results_in_resolved_type_is_rule_name() {
        /* prepare */
        Run run = new Run();
        Tool tool = new Tool();
        ToolComponent driver = new ToolComponent();
        driver.setName("gitleaks");
        tool.setDriver(driver);
        run.setTool(tool);

        ReportingDescriptor rule = new ReportingDescriptor();
        rule.setName("GitHub Personal Access Token");

        /* execute */
        String resolvedType = workaroundToTest.resolveType(rule, run);

        /* test */
        assertEquals(rule.getName(), resolvedType);
    }

    @Test
    void run_tool_driver_name_is_NOT_gitleaks_results_in_resolved_type_is_null() {
        /* prepare */
        Run run = new Run();
        Tool tool = new Tool();
        ToolComponent driver = new ToolComponent();
        driver.setName("random-name");
        tool.setDriver(driver);
        run.setTool(tool);

        ReportingDescriptor rule = new ReportingDescriptor();
        rule.setName("GitHub Personal Access Token");

        /* execute */
        String resolvedType = workaroundToTest.resolveType(rule, run);

        /* test */
        assertNull(resolvedType);
    }

}
