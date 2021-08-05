// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;

@Service
public class ScanProjectConfigAccessLevelService {

    public static final ProjectAccessLevel DEFAULT_ACCESS_LEVEL = ProjectAccessLevel.FULL;

    private static final ScanProjectConfigID CONFIG_ID_PROJECT_ACCESS_LEVEL = ScanProjectConfigID.PROJECT_ACCESS_LEVEL;

    private static final Logger LOG = LoggerFactory.getLogger(ScanProjectConfigAccessLevelService.class);

    @Autowired
    ProjectIdValidation projectIdValidation;

    @Autowired
    ScanProjectConfigService scanprojectConfigService;

    public void changeProjectAccessLevel(String projectId, ProjectAccessLevel newAccessLevel, ProjectAccessLevel formerAccessLevel) {
        /* validate */
        notNull(newAccessLevel, "New accesslevel may not be null!");
        projectIdValidation.validate(projectId);

        /* change configuration */
        ProjectAccessLevel configuredAccessLevel = getProjectAccessLevelOrFallback(projectId, formerAccessLevel);

        if (!Objects.equals(formerAccessLevel, configuredAccessLevel)) {
            /*
             * In this case this could be a race condition (two events on nearly same time
             * happening). So we add a warn log entry.
             */
            LOG.warn("Configured access level was in scan domain:'{}' - but given fallback was: '{}'. This should not happen.", configuredAccessLevel,
                    formerAccessLevel);
        }

        scanprojectConfigService.set(projectId, CONFIG_ID_PROJECT_ACCESS_LEVEL, newAccessLevel.getId());

        LOG.info("Changed access level for project:{} to level:{}", projectId, newAccessLevel.getId());

    }

    /**
     * Fetches project access level for given project - if no project access level
     * is defined, default will be used, which is
     * 
     * @param projectId
     * @return
     */
    public ProjectAccessLevel fetchProjectAccessLevel(String projectId) {
        return getProjectAccessLevelOrFallback(projectId);
    }

    /**
     * Checks if project can be read by normal user operations
     * 
     * @param projectId
     * @return <code>true</code> when read is possible, otherwise <code>false</code>
     */
    public boolean isReadAllowed(String projectId) {
        ProjectAccessLevel accessLevel = getProjectAccessLevelOrFallback(projectId);
        return accessLevel.isEqualOrHigherThan(ProjectAccessLevel.READ_ONLY);
    }

    /**
     * Checks if project can be written by normal user operations
     * 
     * @param projectId
     * @return <code>true</code> when write is possible, otherwise
     *         <code>false</code>
     */
    public boolean isWriteAllowed(String projectId) {
        ProjectAccessLevel accessLevel = getProjectAccessLevelOrFallback(projectId);
        return accessLevel.isEqualOrHigherThan(ProjectAccessLevel.FULL);
    }

    private ProjectAccessLevel getProjectAccessLevelOrFallback(String projectId) {
        return getProjectAccessLevelOrFallback(projectId, DEFAULT_ACCESS_LEVEL);
    }

    private ProjectAccessLevel getProjectAccessLevelOrFallback(String projectId, ProjectAccessLevel fallback) {
        ProjectAccessLevel defaultValue = fallback;
        if (defaultValue == null) {
            defaultValue = DEFAULT_ACCESS_LEVEL;
            LOG.warn("Given project access level fallback was null - should not happen. Used instead now default :{}", defaultValue.getId());
        }

        ScanProjectConfig config = scanprojectConfigService.getOrCreate(projectId, CONFIG_ID_PROJECT_ACCESS_LEVEL, false, defaultValue.getId());
        ProjectAccessLevel configuredAccessLevel = ProjectAccessLevel.fromId(config.getData());
        return configuredAccessLevel;
    }

}
