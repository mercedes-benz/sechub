// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.schedule.ExecutionResult;
import com.mercedesbenz.sechub.domain.schedule.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.ScheduleAssertService;
import com.mercedesbenz.sechub.test.TestCanaryException;

class SecHubJobInfoForUserServiceTest {

    private SecHubJobInfoForUserService serviceToTest;
    private SecHubJobRepository jobRepository;
    private ScheduleAssertService assertService;

    @BeforeEach
    void beforeEach() {

        jobRepository = mock(SecHubJobRepository.class);
        assertService = mock(ScheduleAssertService.class);

        serviceToTest = new SecHubJobInfoForUserService();

        serviceToTest.jobRepository = jobRepository;
        serviceToTest.assertService = assertService;

    }

    @Test
    void assert_validation_is_done_before_any_repo_interaction() {
        /* prepare */
        doThrow(new TestCanaryException()).when(assertService).assertUserHasAccessToProject("project1");

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.listJobsForProject("project1", 1000, 0));

        /* test */
        verify(assertService).assertProjectIdValid("project1"); // project validation is done before
        verify(assertService).assertProjectAllowsReadAccess("project1"); // project validation is done before

        verify(assertService).assertUserHasAccessToProject("project1"); // called and failed...

        verifyNoInteractions(jobRepository); // not called because failed before

    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 33, 100 })
    void job_repository_is_called_with_pageable_limit_of_parameter_when_valid(int limit) {
        /* prepare */
        @SuppressWarnings("unchecked")
        Page<ScheduleSecHubJob> page = mock(Page.class);

        List<ScheduleSecHubJob> list = new ArrayList<>();
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(page);
        serviceToTest.postConstruct();

        /* execute */
        serviceToTest.listJobsForProject("project1", limit, 0);

        /* test */
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(), pageableCaptor.capture());

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

    @ParameterizedTest
    @ValueSource(ints = { -100, -1, 0 })
    void limit_lower_than_1_is_converted_to_1(int limit) {
        /* prepare */
        @SuppressWarnings("unchecked")
        Page<ScheduleSecHubJob> page = mock(Page.class);

        List<ScheduleSecHubJob> list = new ArrayList<>();
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        serviceToTest.postConstruct();

        /* execute */
        serviceToTest.listJobsForProject("project1", limit, 0);

        /* test */
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(), pageableCaptor.capture());

        // test given parameters are as expected
        Pageable value = pageableCaptor.getValue();
        assertEquals(0, value.getPageNumber());// first page...
        assertEquals(1, value.getPageSize()); // fallback to min...

    }

    @ParameterizedTest
    @ValueSource(ints = { 44, 55, 100 })
    void limit_higher_than_max_is_converted_to_max(int limit) {
        /* prepare */
        @SuppressWarnings("unchecked")
        Page<ScheduleSecHubJob> page = mock(Page.class);

        List<ScheduleSecHubJob> list = new ArrayList<>();
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        // define given limit always bigger than defined max - so fallback to max must
        // be used.
        int allowedMaxValue = limit - 1;
        serviceToTest.maximumSize = allowedMaxValue;
        serviceToTest.postConstruct();

        /* execute */
        serviceToTest.listJobsForProject("project1", limit, 0);

        /* test */
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(), pageableCaptor.capture());

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

    @Test
    void sechub_job_entries_returned_by_job_repo_are_base_for_result() {

        /* prepare */

        List<ScheduleSecHubJob> list = new ArrayList<>();
        ScheduleSecHubJob job1 = createJob(TrafficLight.GREEN, ExecutionResult.OK, ExecutionState.ENDED);
        ScheduleSecHubJob job2 = createJob(TrafficLight.RED, ExecutionResult.FAILED, ExecutionState.CANCELED);
        ScheduleSecHubJob job3 = createJob(TrafficLight.RED, ExecutionResult.OK, ExecutionState.ENDED);
        ScheduleSecHubJob job4 = createJob(null, ExecutionResult.NONE, ExecutionState.STARTED);
        ScheduleSecHubJob job5 = createJob(null, null, ExecutionState.READY_TO_START);
        list.add(job1);
        list.add(job2);
        list.add(job3);
        list.add(job4);
        list.add(job5);

        @SuppressWarnings("unchecked")
        Page<ScheduleSecHubJob> page = mock(Page.class);
        when(page.iterator()).thenReturn(list.iterator());
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        /* execute */
        SecHubJobInfoForUserListPage listPage = serviceToTest.listJobsForProject("project1", 10, 0);

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

    private ScheduleSecHubJob createJob(TrafficLight trafficLight, ExecutionResult result, ExecutionState state) {
        ScheduleSecHubJob sechubJob = new ScheduleSecHubJob();
        sechubJob.uUID = UUID.randomUUID();// simulate generation
        sechubJob.created = LocalDateTime.now().minusMinutes(3); // simulate factory creation timestamp
        sechubJob.setStarted(LocalDateTime.now().minusMinutes(2));
        sechubJob.setEnded(LocalDateTime.now().minusMinutes(1));
        sechubJob.setTrafficLight(trafficLight);

        sechubJob.setOwner("the-owner-id" + System.currentTimeMillis());
        sechubJob.setExecutionResult(result);
        sechubJob.setExecutionState(state);

        return sechubJob;
    }
}
