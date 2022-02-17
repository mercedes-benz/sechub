// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.config;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.ScheduleAssertService;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

@Service
public class SchedulerProjectConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerProjectConfigService.class);

    private static final ProjectAccessLevel DEFAULT_ACCESS_LEVEL = ProjectAccessLevel.FULL;

    @Autowired
    SchedulerProjectConfigRepository repository;

    @Autowired
    ScheduleAssertService assertService;

    /**
     * Change the project access level for given project
     *
     * @param projectId
     * @param newAccessLevel
     * @param expectedFormerAccessLevel
     */
    public void changeProjectAccessLevel(String projectId, ProjectAccessLevel newAccessLevel, ProjectAccessLevel expectedFormerAccessLevel) {
        /* validate */
        notNull(newAccessLevel, "new project access level may not be null!");
        assertService.assertProjectIdValid(projectId);

        /* change config */
        SchedulerProjectConfig config = getOrCreateConfig(projectId);

        ProjectAccessLevel configuredAccessLevel = config.getProjectAccessLevel();
        if (!Objects.equals(configuredAccessLevel, expectedFormerAccessLevel)) {
            LOG.warn("Project {} has configured access level: {} but expected former access level was:{}", projectId, configuredAccessLevel,
                    expectedFormerAccessLevel);
        }

        config.setProjectAccessLevel(newAccessLevel);

        repository.save(config);
    }

    /**
     * Resolve the access level for the given project. If no project configuration
     * exists, it will be automatically created. Created project configurations
     * always have the full access level activated.
     *
     * @param projectId
     * @return project access level
     */
    public ProjectAccessLevel getProjectAccessLevel(String projectId) {
        /* validate */
        assertService.assertProjectIdValid(projectId);

        /* fetch or create config */
        return getOrCreateConfig(projectId).getProjectAccessLevel();
    }

    private SchedulerProjectConfig getOrCreateConfig(String projectId) {
        Optional<SchedulerProjectConfig> config = repository.findById(projectId);
        if (config.isPresent()) {
            return config.get();
        }
        SchedulerProjectConfig newConfig = new SchedulerProjectConfig();
        newConfig.projectId = projectId;
        return repository.save(newConfig);
    }

    public void deleteProjectConfiguration(String projectId) {
        assertService.assertProjectIdValid(projectId);

        repository.deleteById(projectId);

        LOG.info("Deleted project configuration for project {}", projectId);
    }

    /**
     * Checks if project can be read by normal user operations
     *
     * @param projectId
     * @return <code>true</code> when read is possible, otherwise <code>false</code>
     */
    public boolean isReadAllowed(String projectId) {
        assertService.assertProjectIdValid(projectId);

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
        assertService.assertProjectIdValid(projectId);

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

        ProjectAccessLevel configuredAccessLevel = getOrCreateConfig(projectId).getProjectAccessLevel();
        return configuredAccessLevel;
    }

}
