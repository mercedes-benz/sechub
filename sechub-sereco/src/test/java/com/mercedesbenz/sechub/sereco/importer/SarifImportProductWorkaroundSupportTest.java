// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.Tool;
import de.jcup.sarif_2_1_0.model.ToolComponent;

class SarifImportProductWorkaroundSupportTest {

    private SarifImportProductWorkaroundSupport supportToTest = new SarifImportProductWorkaroundSupport();

    @BeforeEach
    void beforeEach() {
        supportToTest.workarounds = new ArrayList<>();
    }

    @Test
    void cannot_resolve_type() {
        /* prepare */
        Run run = new Run();
        Tool tool = new Tool();
        ToolComponent driver = new ToolComponent();
        driver.setName("cannot-be-resolved");
        tool.setDriver(driver);
        run.setTool(tool);

        ReportingDescriptor rule = new ReportingDescriptor();
        rule.setName("Rule name of non-existing rule");

        /* execute */
        String resolvedType = supportToTest.resolveType(rule, run);

        /* test */
        assertNull(resolvedType);
    }

    @Test
    void can_resolve_type_results_in_resolved_type_is_rule_name() {
        /* prepare */
        supportToTest.workarounds.add(new GitleaksSarifImportWorkaround());

        Run run = new Run();
        Tool tool = new Tool();
        ToolComponent driver = new ToolComponent();
        driver.setName("gitleaks");
        tool.setDriver(driver);
        run.setTool(tool);

        ReportingDescriptor rule = new ReportingDescriptor();
        rule.setName("GitHub Personal Access Token");

        /* execute */
        String resolvedType = supportToTest.resolveType(rule, run);

        /* test */
        assertEquals(rule.getName(), resolvedType);
    }

}
