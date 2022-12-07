// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.commons.model.SecHubReportModel;

/**
 * Represents the output of a transformation from a report product (like SERECO)
 * into SecHub public representation (which is used to create reports etc.)
 *
 * @author Albert Tregnaghi
 *
 */
public class ReportTransformationResult extends SecHubReportModel {

    private boolean atLeastOneRealProductResultContained;

    public void setAtLeastOneRealProductResultContained(boolean containingNoProductResultsAtAl) {
        this.atLeastOneRealProductResultContained = containingNoProductResultsAtAl;
    }

    public boolean isAtLeastOneRealProductResultContained() {
        return atLeastOneRealProductResultContained;
    }

}
