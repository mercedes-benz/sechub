// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;

import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Result;
import de.jcup.sarif_2_1_0.model.Run;

public interface SarifImportProductWorkaround {

    /**
     * Resolve type from SARIF rule and SARIF run.
     *
     * @param rule
     * @param run
     * @return Resolve type or <code>null</code> if type could not be resolved by
     *         this workaround.
     */
    public default String resolveType(ReportingDescriptor rule, Run run) {
        return null;
    }

    public default String resolveFindingRevisionId(Result result, Run run) {
        return null;
    }

    public default SerecoSeverity resolveCustomSerecoSeverity(Result result, Run run) {
        return null;
    }
}
