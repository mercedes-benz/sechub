// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.domain.schedule.ScheduleErrorIDConstants.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

import jakarta.validation.Valid;

@Component
public class SecHubJobFactory {

    @Autowired
    UserContextService userContextService;

    @Autowired
    SecHubConfigurationModelSupport modelSupport;

    private static final Logger LOG = LoggerFactory.getLogger(SecHubJobFactory.class);

    /**
     * Creates a new job - but does NO persistence!
     *
     * @param configuration
     * @return job
     */
    @Validated
    public ScheduleSecHubJob createJob(@Valid SecHubConfiguration configuration) {
        String userId = userContextService.getUserId();
        if (userId == null) {
            throw new IllegalStateException("No user logged in - illegal access!");
        }

        ScheduleSecHubJob job = new ScheduleSecHubJob();
        try {
            job.projectId = configuration.getProjectId();
            job.jsonConfiguration = configuration.toJSON();
            job.owner = userId;
            job.created = LocalDateTime.now();

            Set<ScanType> scanTypes = modelSupport.collectPublicScanTypes(configuration);
            job.moduleGroup = ModuleGroup.resolveModuleGroupOrNull(scanTypes);

        } catch (JSONConverterException e) {
            // should never happen, but...
            LOG.error(CRITICAL + "Was not able to create a new job because of toJSON problem?", e);
            throw new IllegalStateException(e);
        }
        return job;
    }

}
