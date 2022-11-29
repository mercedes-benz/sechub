package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.ScheduleAssertService;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserListsJobsForProject;

@Service
public class SecHubJobInfoForUserService {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubJobInfoForUserService.class);

    private static final int DEFAULT_MAXIMUM_LIMIT = 100;
    private static final int MINIMUM = 1;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    ScheduleAssertService assertService;

    @Value("${sechub.project.joblist.limit.max:" + DEFAULT_MAXIMUM_LIMIT + "}")
    @MustBeDocumented
    int maximum = DEFAULT_MAXIMUM_LIMIT;

    @PostConstruct
    void postConstruct() {
        if (maximum < MINIMUM) {
            LOG.warn("Illegal maximum limit defined: {} - will use: {} as fallback.", maximum, DEFAULT_MAXIMUM_LIMIT);
            maximum = DEFAULT_MAXIMUM_LIMIT;
        }
    }

    @UseCaseUserListsJobsForProject(@Step(number = 2, name = "Assert access by service and fetch job informaiton for user"))
    public List<SecHubJobInfoForUser> listJobsForProject(String projectId, int limit) {

        assertService.assertProjectIdValid(projectId);
        assertService.assertProjectAllowsReadAccess(projectId);
        assertService.assertUserHasAccessToProject(projectId);

        if (limit < MINIMUM) {
            limit = 1;
            LOG.warn("Limit was to small, changed to: {}", limit);
        }

        if (limit > maximum) {
            limit = maximum;

            LOG.warn("Limit was too big, changed to: {}", limit);
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Direction.DESC, ScheduleSecHubJob.PROPERTY_CREATED));

        ScheduleSecHubJob probe = new ScheduleSecHubJob();
        // reset predefined fields
        probe.executionResult = null;
        probe.executionState = null;
        // set project id as a filter
        probe.projectId = projectId;

        Example<ScheduleSecHubJob> example = Example.of(probe);
        Page<ScheduleSecHubJob> pageFound = jobRepository.findAll(example, pageable);

        List<SecHubJobInfoForUser> list = new ArrayList<>(pageFound.getSize());

        for (ScheduleSecHubJob job : pageFound) {

            SecHubJobInfoForUser infoForUser = new SecHubJobInfoForUser();
            infoForUser.setJobUUID(job.getUUID());
            infoForUser.setExecutedBy(job.getOwner());

            infoForUser.setCreated(job.getCreated());
            infoForUser.setStarted(job.getStarted());
            infoForUser.setEnded(job.getEnded());

            infoForUser.setExecutionState(job.getExecutionState());
            infoForUser.setExecutionResult(job.getExecutionResult());
            infoForUser.setTrafficLight(job.getTrafficLight());

            list.add(infoForUser);
        }
        return list;
    }

}
