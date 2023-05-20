// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PDSWorkspacePreparationResultCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(PDSWorkspacePreparationResultCalculator.class);

    /**
     * Calculates workspace preparation result
     *
     * @param preparationContext
     * @return result, never <code>null</code>
     */
    public PDSWorkspacePreparationResult calculateResult(PDSWorkspacePreparationContext preparationContext) {
        LOG.debug("Start calculation for context: {}", preparationContext);
        boolean executable = checkEitherNoneAcceptedOrWantedDataAvailableInWorkspace(preparationContext);

        PDSWorkspacePreparationResult result = new PDSWorkspacePreparationResult(executable);

        return result;
    }

    private boolean checkEitherNoneAcceptedOrWantedDataAvailableInWorkspace(PDSWorkspacePreparationContext preparationContext) {

        if (preparationContext.isNoneAccepted()) {
            return true;
        }

        if (preparationContext.isSourceAccepted()) {
            if (preparationContext.isExtractedSourceAvailable()) {
                return true;
            }
        }

        if (preparationContext.isBinaryAccepted()) {
            if (preparationContext.isExtractedBinaryAvailable()) {
                return true;
            }
        }
        return false;
    }
}
