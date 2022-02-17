// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubResult;

/**
 * The result type describes which kind of content is represented inside
 * {@link ScanReport#result}. <br>
 * This is necessary, because before #806 we did store just `SecHubResult`
 * inside the field. But afterwards we store the complete report model as
 * result, to have access to messages and more. To be backward compatible we
 * provide this column to distinguish between the different types.
 *
 * @return type as simple integer
 */
public enum ScanReportResultType {

    /**
     * The result contains only {@link SecHubResult} - old style which contains no
     * messages or status information. Will only be used for backward compatibility
     * with old reports
     */
    RESULT,

    /**
     * The result contains complete {@link SecHubReportModel}
     */
    MODEL,
}
