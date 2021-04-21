// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static com.daimler.sechub.domain.schedule.ScheduleErrorIDConstants.*;

import java.time.LocalDateTime;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;

@Component
public class SecHubJobFactory {

    @Autowired
    UserContextService userContextService;

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
        } catch (JSONConverterException e) {
            // should never happen, but...
            LOG.error(CRITICAL + "Was not able to create a new job because of toJSON problem?", e);
            throw new IllegalStateException(e);
        }
        return job;
    }

}
