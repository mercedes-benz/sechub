// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import java.io.InputStream;

import com.daimler.sechub.adapter.AdapterConfig;

public interface CheckmarxAdapterConfig extends AdapterConfig {

	String getTeamIdForNewProjects();

	InputStream getSourceCodeZipFileInputStream();

}