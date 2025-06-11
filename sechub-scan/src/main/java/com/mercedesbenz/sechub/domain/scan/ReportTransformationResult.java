// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;

/**
 * Represents the output of a transformation from a report product (like SERECO)
 * into SecHub public representation (which is used to create reports etc.)
 *
 * Important: Every addition here must be ignored by JSON! Reason: Additions are
 * only interesting at transformation time, but shall not appear inside JSON
 * when transformation result is used inside scan report. Other report model
 * implementations do not have such fields. Normally this would not lead to
 * technical problems when it comes to deserialization (unknown properties are
 * ignored) but we want to avoid possible confusion.
 *
 */
public class ReportTransformationResult extends SecHubReportModel {

    @JsonIgnore
    private boolean atLeastOneRealProductResultContained;
    
    @JsonIgnore
    private boolean atLeastOneProductCanceled;

    public void setAtLeastOneRealProductResultContained(boolean containingNoProductResultsAtAl) {
        this.atLeastOneRealProductResultContained = containingNoProductResultsAtAl;
    }

    public boolean isAtLeastOneRealProductResultContained() {
        return atLeastOneRealProductResultContained;
    }
    
    public void setAtLeastOneProductCanceled(boolean atLeastOneProductCanceled) {
        this.atLeastOneProductCanceled=atLeastOneProductCanceled;
    }
    
    public boolean isAtLeastOneProductCanceled() {
        return atLeastOneProductCanceled;
    }

}
