// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.ScheduleAssertService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationMetaDataMapTransformer;
import com.mercedesbenz.sechub.test.TestCanaryException;

class SecHubJobInfoForUserServiceTest {

    private SecHubJobInfoForUserService serviceToTest;
    private SecHubJobRepository jobRepository;
    private ScheduleAssertService assertService;
    private SecHubConfigurationModelAccessService configurationModelAccess;

    @BeforeEach
    void beforeEach() {

        jobRepository = mock(SecHubJobRepository.class);
        assertService = mock(ScheduleAssertService.class);

        configurationModelAccess = mock(SecHubConfigurationModelAccessService.class);

        serviceToTest = new SecHubJobInfoForUserService();

        serviceToTest.jobRepository = jobRepository;
        serviceToTest.assertService = assertService;
        serviceToTest.metaDataTransformer = new SecHubConfigurationMetaDataMapTransformer();
        serviceToTest.modelValidator = new SecHubConfigurationModelValidator();
        serviceToTest.configurationModelAccess = configurationModelAccess;

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void assert_validation_is_done_before_any_repo_interaction(boolean withMetaData) {
        /* prepare */
        doThrow(new TestCanaryException()).when(assertService).assertUserHasAccessToProject("project1");

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.listJobsForProject("project1", 1000, 0, withMetaData, new HashMap<>()));

        /* test */
        verify(assertService).assertProjectIdValid("project1"); // project validation is done before
        verify(assertService).assertProjectAllowsReadAccess("project1"); // project validation is done before

        verify(assertService).assertUserHasAccessToProject("project1"); // called and failed...

        verifyNoInteractions(jobRepository); // not called because failed before

    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(ints = { 1, 33, 100 })
    void job_repository_is_called_with_pageable_limit_of_parameter_when_valid(int limit) {
        /* prepare */
        Page<ScheduleSecHubJob> page = mock(Page.class);

        List<ScheduleSecHubJob> list = new ArrayList<>();
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        serviceToTest.postConstruct();

        /* execute */
        serviceToTest.listJobsForProject("project1", limit, 0, false, new HashMap<>());

        /* test */
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(Specification.class), pageableCaptor.capture());

        // test given parameters are as expected
        Pageable value = pageableCaptor.getValue();
        assertEquals(0, value.getPageNumber());// first page...
        assertEquals(limit, value.getPageSize());

        // test sorting and ordering
        Sort sort = value.getSort();
        assertNotNull(sort);

        assertTrue(sort.isSorted());
        Order order = sort.getOrderFor(ScheduleSecHubJob.PROPERTY_CREATED);
        assertNotNull(order);
        assertTrue(order.isDescending(), "Order must be descending - means newer on top!");

    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(ints = { -100, -1, 0 })
    void limit_lower_than_1_is_converted_to_1(int limit) {
        /* prepare */
        Page<ScheduleSecHubJob> page = mock(Page.class);

        List<ScheduleSecHubJob> list = new ArrayList<>();
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        serviceToTest.postConstruct();

        /* execute */
        serviceToTest.listJobsForProject("project1", limit, 0, false, new HashMap<>());

        /* test */
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(Specification.class), pageableCaptor.capture());

        // test given parameters are as expected
        Pageable value = pageableCaptor.getValue();
        assertEquals(0, value.getPageNumber());// first page...
        assertEquals(1, value.getPageSize()); // fallback to min...

    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(ints = { 44, 55, 100 })
    void limit_higher_than_max_is_converted_to_max(int limit) {
        /* prepare */
        Page<ScheduleSecHubJob> page = mock(Page.class);

        List<ScheduleSecHubJob> list = new ArrayList<>();
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // define given limit always bigger than defined max - so fallback to max must
        // be used.
        int allowedMaxValue = limit - 1;
        serviceToTest.maximumSize = allowedMaxValue;
        serviceToTest.postConstruct();

        /* execute */
        serviceToTest.listJobsForProject("project1", limit, 0, false, new HashMap<>());

        /* test */
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(Specification.class), pageableCaptor.capture());

        // test given parameters are as expected
        Pageable value = pageableCaptor.getValue();
        assertEquals(0, value.getPageNumber());// first page...
        assertEquals(allowedMaxValue, value.getPageSize()); // fallback to max value

    }

    @ParameterizedTest
    @ValueSource(ints = { -100, -1, 0 })
    void when_max_is_accidently_configured_lower_than_1_after_postConstruct_100_is_used_as_fallback(int limit) {
        /* prepare */
        serviceToTest.maximumSize = limit;

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        assertEquals(100, serviceToTest.maximumSize);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void sechub_job_entries_returned_by_job_repo_are_base_for_result__jobs_have_metadata(boolean withMetaData) {
        createEntriesAndAssertMetaDataAvailableOrNot(withMetaData, true);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void sechub_job_entries_returned_by_job_repo_are_base_for_result__jobs_have_no_metadata(boolean withMetaData) {
        createEntriesAndAssertMetaDataAvailableOrNot(withMetaData, false);
    }

    @SuppressWarnings("unchecked")
    private void createEntriesAndAssertMetaDataAvailableOrNot(boolean withMetaData, boolean createdJobsHaveMetaDataInside) {
        /* prepare */

        List<ScheduleSecHubJob> list = new ArrayList<>();
        ScheduleSecHubJob job1 = createJob(TrafficLight.GREEN, ExecutionResult.OK, ExecutionState.ENDED, createdJobsHaveMetaDataInside);
        ScheduleSecHubJob job2 = createJob(TrafficLight.RED, ExecutionResult.FAILED, ExecutionState.CANCELED, createdJobsHaveMetaDataInside);
        ScheduleSecHubJob job3 = createJob(TrafficLight.RED, ExecutionResult.OK, ExecutionState.ENDED, createdJobsHaveMetaDataInside);
        ScheduleSecHubJob job4 = createJob(null, ExecutionResult.NONE, ExecutionState.STARTED, createdJobsHaveMetaDataInside);
        ScheduleSecHubJob job5 = createJob(null, null, ExecutionState.READY_TO_START, createdJobsHaveMetaDataInside);
        list.add(job1);
        list.add(job2);
        list.add(job3);
        list.add(job4);
        list.add(job5);

        Page<ScheduleSecHubJob> page = mock(Page.class);
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        /* execute */
        SecHubJobInfoForUserListPage listPage = serviceToTest.listJobsForProject("project1", 10, 0, withMetaData, new HashMap<>());

        /* test */
        List<SecHubJobInfoForUser> content = listPage.getContent();
        assertEquals(5, content.size());
        Iterator<SecHubJobInfoForUser> iterator = content.iterator();
        SecHubJobInfoForUser info1 = iterator.next();
        SecHubJobInfoForUser info2 = iterator.next();
        SecHubJobInfoForUser info3 = iterator.next();
        SecHubJobInfoForUser info4 = iterator.next();
        SecHubJobInfoForUser info5 = iterator.next();

        assertInfoBasedOnJob(info1, job1);
        assertInfoBasedOnJob(info2, job2);
        assertInfoBasedOnJob(info3, job3);
        assertInfoBasedOnJob(info4, job4);
        assertInfoBasedOnJob(info5, job5);

        boolean mustHaveMetaData = withMetaData && createdJobsHaveMetaDataInside;

        if (mustHaveMetaData) {
            assertTrue(info1.getMetaData().isPresent());
        } else {
            /* the job has meta data but it is not added here : */
            assertTrue(info1.getMetaData().isEmpty());
        }
    }

    private void assertInfoBasedOnJob(SecHubJobInfoForUser info, ScheduleSecHubJob job) {

        UUID jobUUID = job.getUUID();
        assertNotNull(jobUUID);
        assertEquals(jobUUID, info.getJobUUID());

        ExecutionResult result = job.getExecutionResult();
        assertEquals(result, info.getExecutionResult());

        ExecutionState state = job.getExecutionState();
        assertEquals(state, info.getExecutionState());

        TrafficLight light = job.getTrafficLight();
        assertEquals(light, info.getTrafficLight());// can also be null, so no null check for test data

        String owner = job.getOwner();
        assertNotNull(owner);
        assertEquals(owner, info.getExecutedBy());

        LocalDateTime created = job.getCreated();
        assertNotNull(created);
        assertEquals(created, info.getCreated());

        LocalDateTime ended = job.getEnded();
        assertNotNull(ended);
        assertEquals(ended, info.getEnded());

        LocalDateTime started = job.getStarted();
        assertNotNull(started);
        assertEquals(started, info.getStarted());

    }

    private ScheduleSecHubJob createJob(TrafficLight trafficLight, ExecutionResult result, ExecutionState state, boolean createJobsWithMetaData) {
        ScheduleSecHubJob sechubJob = new ScheduleSecHubJob();
        sechubJob.uUID = UUID.randomUUID();// simulate generation
        sechubJob.created = LocalDateTime.now().minusMinutes(3); // simulate factory creation timestamp
        sechubJob.setStarted(LocalDateTime.now().minusMinutes(2));
        sechubJob.setEnded(LocalDateTime.now().minusMinutes(1));
        sechubJob.setTrafficLight(trafficLight);

        sechubJob.setOwner("the-owner-id" + System.currentTimeMillis());
        sechubJob.setExecutionResult(result);
        sechubJob.setExecutionState(state);

        if (createJobsWithMetaData) {

            SecHubScanConfiguration config = new SecHubScanConfiguration();
            SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
            metaData.getLabels().put("testlabel1", "testvalue1");
            config.setMetaData(metaData);

            // simulate encryption done by job creation factory
            when(configurationModelAccess.resolveUnencryptedConfiguration(sechubJob)).thenReturn(config);
        }

        return sechubJob;
    }
}
