package com.mercedesbenz.sechub.domain.administration.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobInformationListServiceTest {

    private JobInformationListService serviceToTest;
    private JobInformationRepository repository;

    @BeforeEach
    void beforeEach() {

        repository = mock(JobInformationRepository.class);

        serviceToTest = new JobInformationListService();
        serviceToTest.repository = repository;
    }

    @Test
    void when_no_running_jobs_are_found_in_repository_an_empty_list_is_returned() {
        /* prepare */
        when(repository.findAllRunningJobs()).thenReturn(Collections.emptyList());

        /* execute */
        List<JobInformationListEntry> result = serviceToTest.fetchRunningJobs();

        /* test */
        assertNotNull(result);
        assertEquals(0,result.size());
    }

    @Test
    void when_a_job_information_is_found_in_repository_an_enty_with_job_information_data_is_returned() {

        /* prepare */
        JobInformation information1 = new JobInformation();
        information1.configuration = "config";
        information1.info = "info";
        information1.jobUUID = UUID.randomUUID();
        information1.owner = "owner";
        information1.projectId = "projectId";
        information1.since = LocalDateTime.now().minusMinutes(10);
        information1.status = JobStatus.RUNNING;

        when(repository.findAllRunningJobs()).thenReturn(List.of(information1));

        /* execute */
        List<JobInformationListEntry> result = serviceToTest.fetchRunningJobs();

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());

        Iterator<JobInformationListEntry> iterator = result.iterator();
        assertEntryContainsInfo(information1, iterator.next());
    }

    @Test
    void when_2_job_information_entries_found_in_repository_two_enty_with_job_information_data_is_returned() {

        /* prepare */
        JobInformation information1 = new JobInformation();
        information1.configuration = "config1";
        information1.info = "info1";
        information1.jobUUID = UUID.randomUUID();
        information1.owner = "owner1";
        information1.projectId = "projectId1";
        information1.since = LocalDateTime.now().minusMinutes(10);
        information1.status = JobStatus.RUNNING;

        JobInformation information2 = new JobInformation();
        information2.configuration = "config2";
        information2.info = "info2";
        information2.jobUUID = UUID.randomUUID();
        information2.owner = "owner2";
        information2.projectId = "projectId2";
        information2.since = LocalDateTime.now().minusMinutes(5);
        information2.status = JobStatus.CREATED; // just a changed value - makes no sense here, but we use it as an example

        when(repository.findAllRunningJobs()).thenReturn(List.of(information1, information2));

        /* execute */
        List<JobInformationListEntry> result = serviceToTest.fetchRunningJobs();

        /* test */
        assertNotNull(result);
        assertEquals(2, result.size());

        Iterator<JobInformationListEntry> iterator = result.iterator();
        assertEntryContainsInfo(information1, iterator.next());
        assertEntryContainsInfo(information2, iterator.next());
    }

    private void assertEntryContainsInfo(JobInformation information1, JobInformationListEntry entry1) {
        assertEquals(information1.jobUUID, entry1.jobUUID());
        assertEquals(information1.projectId, entry1.projectId());
        assertEquals(information1.since, entry1.since());
        assertEquals(information1.status, entry1.status());
    }

}
