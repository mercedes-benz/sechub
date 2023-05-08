// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.Map;
import java.util.Optional;

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

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.domain.schedule.ScheduleAssertService;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.configuration.MapToSecHubConfigurationMetaDataTransformer;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserListsJobsForProject;

@Service
public class SecHubJobInfoForUserService {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubJobInfoForUserService.class);

    private static final int DEFAULT_MAXIMUM_LIMIT = 100;
    private static final int DEFAULT_MAXIMUM_PAGE = 100;
    private static final int MINIMUM_SIZE = 1;

    private static final int MINIMUM_PAGE = 0;;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    ScheduleAssertService assertService;
    
    @Autowired
    MapToSecHubConfigurationMetaDataTransformer metaDataTransformer;

    @Value("${sechub.project.joblist.size.max:" + DEFAULT_MAXIMUM_LIMIT + "}")
    @MustBeDocumented("Maximum limit for job information list entries per page")
    int maximumSize = DEFAULT_MAXIMUM_LIMIT;

    @Value("${sechub.project.joblist.page.max:" + DEFAULT_MAXIMUM_PAGE + "}")
    @MustBeDocumented
    int maximumPage = DEFAULT_MAXIMUM_PAGE;

    @PostConstruct
    void postConstruct() {
        if (maximumSize < MINIMUM_SIZE) {
            LOG.warn("Illegal maximum size defined: {} - will use: {} as fallback.", maximumSize, DEFAULT_MAXIMUM_LIMIT);
            maximumSize = DEFAULT_MAXIMUM_LIMIT;
        }
        if (maximumPage < MINIMUM_PAGE) {
            LOG.warn("Illegal maximum page defined: {} - will use: {} as fallback.", maximumPage, DEFAULT_MAXIMUM_PAGE);
            maximumPage = DEFAULT_MAXIMUM_PAGE;
        }
    }

    @UseCaseUserListsJobsForProject(@Step(number = 2, name = "Assert access by service and fetch job information for user"))
    public SecHubJobInfoForUserListPage listJobsForProject(String projectId, int size, int page, boolean resultsContainMetaData, Map<String, String> allParams) {

        assertService.assertProjectIdValid(projectId);
        assertService.assertProjectAllowsReadAccess(projectId);
        assertService.assertUserHasAccessToProject(projectId);
        

        if (size < MINIMUM_SIZE) {
            LOG.warn("Size: {} is to small, will change to: {}", size, MINIMUM_SIZE);
            size = MINIMUM_SIZE;
        }

        if (size > maximumSize) {
            LOG.warn("Size: {} is too big, will change to: {}", size, maximumSize);
            size = maximumSize;

        }

        if (page < MINIMUM_PAGE) {
            LOG.warn("Page:{} was to small, will change to: {}", page, MINIMUM_PAGE);
            page = MINIMUM_PAGE;
        }

        if (page > maximumPage) {
            LOG.warn("Page:{} was too big, will change to: {}", page, maximumPage);
            page = maximumPage;
        }
        
        SecHubConfigurationMetaData metaDataForFiltering = metaDataTransformer.transform(allParams);
        
        return loadDataAndCreateListPage(projectId, size, page, resultsContainMetaData, metaDataForFiltering);
    }

    private SecHubJobInfoForUserListPage loadDataAndCreateListPage(String projectId, int size, int page, boolean withMetaData, SecHubConfigurationMetaData metaDataForFiltering) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, ScheduleSecHubJob.PROPERTY_CREATED));

        ScheduleSecHubJob probe = new ScheduleSecHubJob();
        // reset predefined fields
        probe.executionResult = null;
        probe.executionState = null;
        // set project id as a filter
        probe.projectId = projectId;

        Example<ScheduleSecHubJob> example = Example.of(probe);
        Page<ScheduleSecHubJob> pageFound = jobRepository.findAll(example, pageable);

        return transformToListPage(projectId, pageFound, withMetaData);
    }

    private SecHubJobInfoForUserListPage transformToListPage(String projectId, Page<ScheduleSecHubJob> pageFound, boolean withMetaData) {
        SecHubJobInfoForUserListPage listPage = new SecHubJobInfoForUserListPage();
        listPage.setPage(pageFound.getNumber());
        listPage.setTotalPages(pageFound.getTotalPages());
        listPage.setProjectId(projectId);

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

            listPage.getContent().add(infoForUser);

            if (withMetaData) {
                attachJobMetaData(job, infoForUser);
            }
        }
        return listPage;
    }

    private void attachJobMetaData(ScheduleSecHubJob job, SecHubJobInfoForUser infoForUser) {
        String json = job.getJsonConfiguration();
        if (json == null) {
            LOG.error("No sechub configuration found for job: {}. Cannot resolve meta data!", job.getUUID());
            return;
        }
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(json);
        Optional<SecHubConfigurationMetaData> metaDataOpt = configuration.getMetaData();
        if (metaDataOpt.isPresent()) {
            infoForUser.setMetaData(metaDataOpt.get());
        }
    }

}
