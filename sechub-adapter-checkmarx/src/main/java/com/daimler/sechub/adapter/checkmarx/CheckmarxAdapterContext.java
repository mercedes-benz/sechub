// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import com.daimler.sechub.adapter.AdapterContext;
import com.daimler.sechub.adapter.checkmarx.support.ReportDetails;

public interface CheckmarxAdapterContext extends AdapterContext<CheckmarxAdapterConfig> {

	long getScanId();

	ReportDetails getReportDetails();

	long getReportId();

	CheckmarxAdapter getCheckmarxAdapter();

	void setReportId(long reportId);


}