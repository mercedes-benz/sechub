// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.jcup.sarif_2_1_0.model.*;

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

    @Override
    public String resolveCustomSechubSeverity(Result result, Run run) {
        if (result == null) {
            return null;
        }
        if (!isGitleaksRun(run)) {
            return null;
        }
        List<Location> locations = result.getLocations();
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        PhysicalLocation physicalLocation = locations.get(0).getPhysicalLocation();
        if (physicalLocation == null) {
            return null;
        }
        Region region = physicalLocation.getRegion();
        if (region == null) {
            return null;
        }
        PropertyBag properties = region.getProperties();
        if (properties == null) {
            return null;
        }
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();
        if (additionalProperties == null) {
            return null;
        }
        String severityKey = SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey();
        return (String) additionalProperties.get(severityKey);
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
