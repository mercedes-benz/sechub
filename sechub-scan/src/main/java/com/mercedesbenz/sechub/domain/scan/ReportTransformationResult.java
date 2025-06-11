// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.commons.model.SecHubReportModel;

/**
 * Represents the output of a transformation from a report product (like SERECO)
 * into SecHub public representation (which is used to create reports etc.)
 *
 */
public class ReportTransformationResult {

    private boolean atLeastOneRealProductResultContained;
    private boolean atLeastOneProductCanceled;
    private SecHubReportModel model;

    public ReportTransformationResult() {
        model = new SecHubReportModel();
    }

    public void setAtLeastOneRealProductResultContained(boolean containingNoProductResultsAtAl) {
        this.atLeastOneRealProductResultContained = containingNoProductResultsAtAl;
    }

    public boolean isAtLeastOneRealProductResultContained() {
        return atLeastOneRealProductResultContained;
    }

    public void setAtLeastOneProductCanceled(boolean atLeastOneProductCanceled) {
        this.atLeastOneProductCanceled = atLeastOneProductCanceled;
    }

    public boolean isAtLeastOneProductCanceled() {
        return atLeastOneProductCanceled;
    }

    /**
     * @return report model, never <code>null</code>
     */
    public SecHubReportModel getModel() {
        return model;
    }

}
