// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import com.daimler.sechub.domain.scan.AnyTargetOneInstallSetup;

public interface CheckmarxInstallSetup extends AnyTargetOneInstallSetup {

    /**
     * Provide team id when project has to be created
     * 
     * @param projectId
     * @return team id either by name pattern rule, or default id, but never
     *         <code>null</code>
     */
    public String getTeamIdForNewProjects(String projectId);

    /**
     * Provide presetId when project has to be created
     * 
     * @param projectId
     * @return preset Id or <code>null</code> - in case of null checkmarx will use
     *         default preset
     */
    public Long getPresetIdForNewProjects(String projectId);

    /**
     * Get 'client secret' value
     * 
     * @return
     */
    public String getClientSecret();

    /**
     * Get the engine configuration name
     * 
     * @return
     */
    public String getEngineConfigurationName();
}
