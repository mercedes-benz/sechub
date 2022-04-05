// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import com.mercedesbenz.sechub.adapter.AdapterContext;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxOAuthData;
import com.mercedesbenz.sechub.adapter.checkmarx.support.ReportDetails;

public interface CheckmarxAdapterContext extends AdapterContext<CheckmarxAdapterConfig> {

    long getScanId();

    ReportDetails getReportDetails();

    long getReportId();

    CheckmarxAdapter getCheckmarxAdapter();

    void setReportId(long reportId);

    CheckmarxOAuthData getoAuthData();

    void markAuthenticated(CheckmarxOAuthData data);

}