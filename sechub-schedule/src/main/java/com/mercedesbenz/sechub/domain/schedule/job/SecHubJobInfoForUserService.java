// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;
import com.mercedesbenz.sechub.domain.schedule.ScheduleAssertService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationMetaDataMapTransformer;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserListsJobsForProject;

import jakarta.annotation.PostConstruct;

@Service
public class SecHubJobInfoForUserService {

    public static final int MAXIMUM_ALLOWED_LABEL_PARAMETERS = 10;
    private static final int MAXIMUM_ALLOWED_PARAMETERS = MAXIMUM_ALLOWED_LABEL_PARAMETERS + 4; // projectId, page, size, withMetaData

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
    SecHubConfigurationMetaDataMapTransformer metaDataTransformer;

    @Autowired
    SecHubConfigurationModelValidator modelValidator;

    @Autowired
    SecHubConfigurationModelAccessService configurationModelAccess;

    @Value("${sechub.project.joblist.size.max:" + DEFAULT_MAXIMUM_LIMIT + "}")
    @MustBeDocumented(value = "Maximum limit for job information list entries per page", scope = SCOPE_JOB)
    int maximumSize = DEFAULT_MAXIMUM_LIMIT;

    @Value("${sechub.project.joblist.page.max:" + DEFAULT_MAXIMUM_PAGE + "}")
    @MustBeDocumented(scope = SCOPE_JOB)
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
    public SecHubJobInfoForUserListPage listJobsForProject(String projectId, int size, int page, boolean resultsShallContainMetaData,
            Map<String, String> allParams) {

        assertService.assertProjectIdValid(projectId);
        assertService.assertProjectAllowsReadAccess(projectId);
        assertService.assertUserHasAccessToProject(projectId);

        assertNotTooManyParameters(allParams);

        SearchContext searchContext = new SearchContext();
        searchContext.projectId = projectId;
        searchContext.page = page;
        searchContext.size = size;
        searchContext.resultsShallContainMetaData = resultsShallContainMetaData;

        ensureValidSearchParameters(searchContext);

        handleJobDataParametersIfExisting(allParams, searchContext);

        return loadDataAndCreateListPage(searchContext);
    }

    private SecHubJobInfoForUserListPage loadDataAndCreateListPage(SearchContext searchContext) {
        Pageable pageable = PageRequest.of(searchContext.page, searchContext.size, Sort.by(Direction.DESC, ScheduleSecHubJob.PROPERTY_CREATED));

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(searchContext.projectId, searchContext.filterData);

        Page<ScheduleSecHubJob> pageFound = jobRepository.findAll(specification, pageable);
        return transformToListPage(pageFound, searchContext);
    }

    private void ensureValidSearchParameters(SearchContext searchContext) {
        ensureValidSize(searchContext);
        ensureValidPage(searchContext);
    }

    private void ensureValidSize(SearchContext searchContext) {
        int size = searchContext.size;

        if (size < MINIMUM_SIZE) {
            LOG.warn("Size: {} is too small, will change to: {}", size, MINIMUM_SIZE);
            searchContext.size = MINIMUM_SIZE;
        }

        if (size > maximumSize) {
            LOG.warn("Size: {} is too big, will change to: {}", size, maximumSize);
            searchContext.size = maximumSize;

        }
    }

    private void ensureValidPage(SearchContext searchContext) {
        int page = searchContext.page;
        if (page < MINIMUM_PAGE) {
            LOG.warn("Page: {} was too small, will change to: {}", page, MINIMUM_PAGE);
            searchContext.page = MINIMUM_PAGE;
        }

        if (page > maximumPage) {
            LOG.warn("Page: {} was too big, will change to: {}", page, maximumPage);
            searchContext.page = maximumPage;
        }
    }

    private void handleJobDataParametersIfExisting(Map<String, String> allParams, SearchContext context) {
        SecHubConfigurationMetaData metaDataForFiltering = metaDataTransformer.transform(allParams);

        Map<String, String> labels = metaDataForFiltering.getLabels();
        if (labels.size() > MAXIMUM_ALLOWED_LABEL_PARAMETERS) {
            throw new BadRequestException("Maximum of allowed label parameters reached: " + MAXIMUM_ALLOWED_LABEL_PARAMETERS);
        }

        SecHubConfigurationModelValidationResult result = modelValidator.validateMetaDataLabels(labels);
        if (result.hasErrors()) {
            throw new BadRequestException(result.getErrors().iterator().next().getMessage());
        }
        /* we transform back to have only valid parameter data */
        Map<String, String> validFilterData = metaDataTransformer.transform(metaDataForFiltering);
        context.filterData = validFilterData;

    }

    private SecHubJobInfoForUserListPage transformToListPage(Page<ScheduleSecHubJob> pageFound, SearchContext searchContext) {
        SecHubJobInfoForUserListPage listPage = new SecHubJobInfoForUserListPage();

        listPage.setPage(pageFound.getNumber());
        listPage.setTotalPages(pageFound.getTotalPages());
        listPage.setProjectId(searchContext.projectId);

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

            if (searchContext.resultsShallContainMetaData) {
                attachJobMetaData(job, infoForUser);
            }
        }
        return listPage;
    }

    private void attachJobMetaData(ScheduleSecHubJob job, SecHubJobInfoForUser infoForUser) {

        // we fetch the unencrypted configuration - but we do only store meta data which
        // contains no
        // sensitive information.
        SecHubConfigurationModel configuration = configurationModelAccess.resolveUnencryptedConfiguration(job);
        if (configuration == null) {
            LOG.error("No sechub configuration found for job: {}. Cannot resolve meta data!", job.getUUID());
            return;
        }

        Optional<SecHubConfigurationMetaData> metaDataOpt = configuration.getMetaData();
        if (metaDataOpt.isPresent()) {
            infoForUser.setMetaData(metaDataOpt.get());
        }
    }

    private void assertNotTooManyParameters(Map<String, String> allParams) {
        if (allParams.size() > MAXIMUM_ALLOWED_PARAMETERS) {
            throw new BadRequestException("Too many parameters used. Maximum allowed parameters:" + MAXIMUM_ALLOWED_PARAMETERS);
        }

    }

    private class SearchContext {
        private String projectId;
        private int size;
        int page;
        boolean resultsShallContainMetaData;
        private Map<String, String> filterData;
    }

}
