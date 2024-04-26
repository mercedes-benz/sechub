// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.Result;
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
        return visitAllWorkaroundsAndUseFirstResultNotNull(rule, run, new WorkaroundVisitor<String, ReportingDescriptor>() {

            @Override
            public String visit(ReportingDescriptor element, Run run, SarifImportProductWorkaround workaround) {
                return workaround.resolveType(rule, run);
            }
        });
    }

    public String resolveFindingRevisionId(Result result, Run run) {
        return visitAllWorkaroundsAndUseFirstResultNotNull(result, run, new WorkaroundVisitor<String, Result>() {

            @Override
            public String visit(Result element, Run run, SarifImportProductWorkaround workaround) {
                return workaround.resolveFindingRevisionId(result, run);
            }
        });
    }

    private <R, E> R visitAllWorkaroundsAndUseFirstResultNotNull(E element, Run run, WorkaroundVisitor<R, E> visitor) {
        for (SarifImportProductWorkaround workaround : workarounds) {
            R result = visitor.visit(element, run, workaround);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public interface WorkaroundVisitor<R, E> {

        public R visit(E element, Run run, SarifImportProductWorkaround workaround);
    }

}
