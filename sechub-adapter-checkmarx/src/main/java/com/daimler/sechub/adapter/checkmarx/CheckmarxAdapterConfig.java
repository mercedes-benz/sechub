// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import java.io.InputStream;

import com.daimler.sechub.adapter.AdapterConfig;

public interface CheckmarxAdapterConfig extends AdapterConfig {

    /**
     *
     * @return team id, never <code>null</code>
     */
    String getTeamIdForNewProjects();

    /**
     *
     * @return preset id or <code>null</code>
     */
    Long getPresetIdForNewProjectsOrNull();

    InputStream getSourceCodeZipFileInputStream();

    String getClientSecret();

    String getEngineConfigurationName();

    /**
     * @return <code>true</code> when checkmarx shall always do a fullscan - not
     *         only for new projects
     */
    public boolean isAlwaysFullScanEnabled();
}