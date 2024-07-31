// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.util.Map;

import org.springframework.stereotype.Component;

import de.jcup.sarif_2_1_0.model.PartialFingerprints;
import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Result;
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

    @Override
    public String resolveFindingRevisionId(Result result, Run run) {
        if (result == null) {
            return null;
        }
        if (isGitleaksRun(run)) {
            PartialFingerprints partialFingerprints = result.getPartialFingerprints();
            if (partialFingerprints != null) {
                Map<String, String> properties = partialFingerprints.getAdditionalProperties();
                String commitSha = properties.get("commitSha");
                return commitSha;
            }
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
