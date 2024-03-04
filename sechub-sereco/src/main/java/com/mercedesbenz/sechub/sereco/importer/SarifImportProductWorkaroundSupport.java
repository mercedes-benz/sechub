// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Run;

/**
 * Support to handle any kind of workaround for SARIF imports.
 */
@Component
public class SarifImportProductWorkaroundSupport {

    @Autowired
    List<SarifImportProductWorkaround> workarounds = new ArrayList<>();

    /**
     * Resolve type from SARIF rule and SARIF run.
     *
     * @param rule
     * @param run
     * @return Resolved type or <code>null</code> if type could not be resolved by
     *         any available workaround.
     */
    public String resolveType(ReportingDescriptor rule, Run run) {
        // iterate over workarounds, first non null result will be returned
        for (SarifImportProductWorkaround workaround : workarounds) {
            String resolvedType = workaround.resolveType(rule, run);
            if (resolvedType != null) {
                return resolvedType;
            }
        }
        return null;
    }
}
