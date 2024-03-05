// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import org.springframework.stereotype.Component;

import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.Tool;
import de.jcup.sarif_2_1_0.model.ToolComponent;

@Component
public class GitleaksSarifImportWorkaround implements SarifImportProductWorkaround {

    @Override
    public String resolveType(ReportingDescriptor rule, Run run) {
        if (rule == null) {
            return null;
        }
        if (isGitleaksRun(run)) {
            return rule.getName();
        }
        return null;
    }

    private boolean isGitleaksRun(Run run) {
        if (run == null) {
            return false;
        }
        Tool tool = run.getTool();
        if (tool == null) {
            return false;
        }
        ToolComponent driver = tool.getDriver();
        if (driver == null) {
            return false;
        }
        return "gitleaks".equalsIgnoreCase(driver.getName());
    }
}
