// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.commons.model.job.ExecutionState.*;
import static com.mercedesbenz.sechub.domain.schedule.job.JobCreator.*;
import static com.mercedesbenz.sechub.test.FlakyOlderThanTestWorkaround.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.test.TestUtil;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = { SecHubJobRepository.class, SecHubJobRepositoryDBTest.SimpleTestConfiguration.class })
public class SecHubJobRepositoryDBTest {

    private static final Set<Long> FIRST_ENCRYPTION_POOL_ENTRY_ONLY = Set.of(0L);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SecHubJobRepository jobRepository;

    private JobCreator jobCreator;

    @BeforeEach
    void before() {
        jobCreator = jobCreator("p0", entityManager);
    }

    @Test
    void collectAllUsedEncryptionPoolIdsInsideJobs_no_jobs_found() {
        /* execute */
        List<Long> result = jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs();

        /* test */
        assertThat(result).isNotNull().isEmpty();
    }

    @ParameterizedTest
    @EnumSource(ExecutionState.class)
    void collectAllUsedEncryptionPoolIdsInsideJobs_only_two_jobs_found_same_poolid(ExecutionState stateOfJob) {
        /* prepare */
        int poolId = 1234;
        createJobUsingEncryptionPoolId(poolId, stateOfJob);
        createJobUsingEncryptionPoolId(poolId, stateOfJob);

        /* execute */
        List<Long> result = jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs();

        /* test */
        assertThat(result).isNotNull().hasSize(1);
        Long value = result.iterator().next();

        assertThat(value).isEqualTo(poolId);
    }

    @Test
    void collectAllUsedEncryptionPoolIdsInsideJobs_multiple_jobs_mixed_poolids() {
        /* prepare */
        createJobUsingEncryptionPoolId(0, ExecutionState.INITIALIZING);
        createJobUsingEncryptionPoolId(1, ExecutionState.READY_TO_START);
        createJobUsingEncryptionPoolId(1, ExecutionState.CANCEL_REQUESTED);
        createJobUsingEncryptionPoolId(3, ExecutionState.ENDED);
        createJobUsingEncryptionPoolId(176, ExecutionState.STARTED);
        createJobUsingEncryptionPoolId(176, ExecutionState.CANCELED);

        /* execute */
        List<Long> result = jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs();

        /* test */
        assertThat(result).isNotNull().hasSize(4).contains(0L, 1L, 3L, 176L);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 10 })
    void countCanceledOrEndedJobsWithEncryptionPoolIdLowerThan_works_as_expected(int expectedResultCount) {
        /* prepare */
        jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.INITIALIZING).create();
        jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();

        // generate data
        if (expectedResultCount > 0) {
            jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        }
        if (expectedResultCount > 1) {
            for (int i = 1; i < expectedResultCount; i++) {
                jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();
            }
        }

        /* execute */
        long result = jobRepository.countCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(1L);

        /* test */
        assertEquals(result, expectedResultCount);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 4 })
    void nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan_one_ended_only_single_entry_always_returned(int amount) {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.INITIALIZING).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();

        // check preconditions
        assertEquals(0, newJob1.getEncryptionCipherPoolId());
        assertEquals(0, newJob2.getEncryptionCipherPoolId());
        assertEquals(0, newJob3.getEncryptionCipherPoolId());
        assertEquals(0, newJob4.getEncryptionCipherPoolId());
        assertEquals(0, newJob5.getEncryptionCipherPoolId());

        /* execute */
        List<ScheduleSecHubJob> list = jobRepository.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(1L, amount);

        /* test */
        assertFalse(list.isEmpty());
        assertTrue(list.contains(newJob4)); // only this, because others are in wrong state
        assertEquals(1, list.size());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 4 })
    void nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan_one_is_lower_ended_only_single_entry_always_returned(int amount) {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).encryptionPoolId(0L).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).encryptionPoolId(1L).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).encryptionPoolId(1L).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p2").module(ModuleGroup.STATIC).encryptionPoolId(2L).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p2").module(ModuleGroup.STATIC).encryptionPoolId(3L).being(ExecutionState.ENDED).create();

        // check preconditions
        assertEquals(0, newJob1.getEncryptionCipherPoolId());
        assertEquals(1, newJob2.getEncryptionCipherPoolId());
        assertEquals(1, newJob3.getEncryptionCipherPoolId());
        assertEquals(2, newJob4.getEncryptionCipherPoolId());
        assertEquals(3, newJob5.getEncryptionCipherPoolId());

        /* execute */
        List<ScheduleSecHubJob> list = jobRepository.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(1L, amount);

        /* test */
        assertFalse(list.isEmpty());
        assertTrue(list.contains(newJob1)); // only this, because others are already with higher pool id
        assertEquals(1, list.size());
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 10, 100 })
    void nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan_one_ended_one_canceled_entries_always_returned(int amount) {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.INITIALIZING).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob6 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();

        // check preconditions
        assertEquals(0, newJob1.getEncryptionCipherPoolId());
        assertEquals(0, newJob2.getEncryptionCipherPoolId());
        assertEquals(0, newJob3.getEncryptionCipherPoolId());
        assertEquals(0, newJob4.getEncryptionCipherPoolId());
        assertEquals(0, newJob5.getEncryptionCipherPoolId());
        assertEquals(0, newJob6.getEncryptionCipherPoolId());

        /* execute */
        List<ScheduleSecHubJob> list = jobRepository.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(1L, amount);

        /* test */
        assertFalse(list.isEmpty());
        assertTrue(list.contains(newJob4));
        assertTrue(list.contains(newJob6));
        assertEquals(2, list.size());
    }

    @Test
    void nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan_randomization_works() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob6 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();

        // check preconditions
        assertEquals(0, newJob1.getEncryptionCipherPoolId());
        assertEquals(0, newJob2.getEncryptionCipherPoolId());
        assertEquals(0, newJob3.getEncryptionCipherPoolId());
        assertEquals(0, newJob4.getEncryptionCipherPoolId());
        assertEquals(0, newJob5.getEncryptionCipherPoolId());
        assertEquals(0, newJob6.getEncryptionCipherPoolId());

        Map<UUID, AtomicInteger> map = new LinkedHashMap<>();

        /* execute */
        for (int i = 0; i < 30; i++) {
            List<ScheduleSecHubJob> list = jobRepository.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(1L, 1);
            for (ScheduleSecHubJob job : list) {
                AtomicInteger atomic = map.computeIfAbsent(job.getUUID(), (uuid) -> new AtomicInteger(0));
                atomic.incrementAndGet();
            }
        }

        /* test */
        assertEquals(6, map.size()); // when calling n times, we expect every entry is contained
        System.out.println("map:" + map);

    }

    @Test
    void nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan_2_entries_always_returned() {
        /* prepare */
        jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();

        /* execute */
        List<ScheduleSecHubJob> list = jobRepository.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(1L, 10);

        /* test */
        assertFalse(list.isEmpty());
        assertTrue(list.contains(newJob4));
    }

    @Test
    void markJobsAsSuspended() {
        /* prepare */
        String projectId = "p1";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        entityManager.persist(job1);

        ScheduleSecHubJob job2 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        entityManager.persist(job2);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime ended = LocalDateTime.of(2024, 9, 23, 17, 42, 00);// we want to avoid storage round problems in test

        /* execute */
        jobRepository.markJobsAsSuspended(Set.of(job1.getUUID()), ended);

        /* test */
        Optional<ScheduleSecHubJob> job1Loaded = jobRepository.findById(job1.getUUID());
        Optional<ScheduleSecHubJob> job2Loaded = jobRepository.findById(job2.getUUID());

        assertEquals(ExecutionState.SUSPENDED, job1Loaded.get().getExecutionState());
        assertEquals(ExecutionState.READY_TO_START, job2Loaded.get().getExecutionState());

        assertEquals(ended, job1Loaded.get().getEnded());
        assertNull(job2Loaded.get().getEnded());

    }

    @Test
    void markJobsAsSuspended_multi() {
        /* prepare */
        String projectId = "p1";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        entityManager.persist(job1);

        ScheduleSecHubJob job2 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        entityManager.persist(job2);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime ended = LocalDateTime.of(2024, 9, 23, 17, 42, 10); // we want to avoid storage round problems in test

        /* execute */
        jobRepository.markJobsAsSuspended(Set.of(job1.getUUID(), job2.getUUID()), ended);

        /* test */
        Optional<ScheduleSecHubJob> job1Loaded = jobRepository.findById(job1.getUUID());
        Optional<ScheduleSecHubJob> job2Loaded = jobRepository.findById(job2.getUUID());

        assertEquals(ExecutionState.SUSPENDED, job1Loaded.get().getExecutionState());
        assertEquals(ExecutionState.SUSPENDED, job2Loaded.get().getExecutionState());

        assertEquals(ended, job1Loaded.get().getEnded());
        assertEquals(ended, job2Loaded.get().getEnded());
    }

    @Test
    void findAll_with_specifications_for_project_id_and_data_2_data_but_only_one_matches() {

        /* prepare */
        String projectId = "p1";
        String acceptedKey = "testkey1";
        String value = "testvalue1";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData(acceptedKey, value);
        entityManager.persist(job1);

        ScheduleSecHubJob job2 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job2.addData("other", value);
        entityManager.persist(job2);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put(acceptedKey, value);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(projectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(1, results.size());
        assertTrue(results.contains(job1));
    }

    @Test
    void findAll_with_specifications_for_project_id_and_data_1_data_but_project_id_does_not_match() {

        /* prepare */
        String projectId = "p1";
        String searchProjectId = "p2";

        String acceptedKey = "testkey1";
        String value = "testvalue1";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData(acceptedKey, value);
        entityManager.persist(job1);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put(acceptedKey, value);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(searchProjectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(0, results.size());
    }

    @Test
    void findAll_with_specifications_for_project_id_and_data_1_data_value_matches_but_not_key() {

        /* prepare */
        String projectId = "p1";
        String sameValue = "testvalue1";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData("testkey1", sameValue);
        entityManager.persist(job1);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put("other", sameValue);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(projectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(0, results.size());

    }

    @Test
    void findAll_with_specifications_for_project_id_and_data_2_data_both_matching() {

        /* prepare */
        String projectId = "p1";
        String searchKey = "testkey3-common";
        String value = "testvalue3";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData("other1", value);
        job1.addData(searchKey, value);
        entityManager.persist(job1);

        ScheduleSecHubJob job2 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job2.addData("other2", value);
        job2.addData(searchKey, value);
        entityManager.persist(job2);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put(searchKey, value);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(projectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(2, results.size());
        assertTrue(results.contains(job1));
        assertTrue(results.contains(job2));

    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "other", "complete-value-with-all+", "%complete%" })
    void findAll_with_specifications_for_project_id_and_data_1_data_search_with_wrong_value(String wrongSearchValue) {

        /* prepare */
        String projectId = "p1";
        String value = "complete-value-with-all";
        String key = "testkey-common";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData(key, value);
        entityManager.persist(job1);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put(key, wrongSearchValue);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(projectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(0, results.size());

    }

    @Test
    void findAll_with_specifications_for_project_id_and_data_2_data_both_matching_in_key_but_not_value() {

        /* prepare */
        String projectId = "p1";
        String key = "testkey-common";
        String value = "testvalue";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData(key, "other");
        entityManager.persist(job1);

        ScheduleSecHubJob job2 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job2.addData(key, value);
        entityManager.persist(job2);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put(key, value);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(projectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(1, results.size());
        assertTrue(results.contains(job2));

    }

    @Test
    void findAll_with_specifications_for_project_id_and_data_3_data_2_search_variants_only_2_matching() {

        /* prepare */
        String projectId = "p1";
        String key = "testkey-common";
        String value = "testvalue";

        // persist data
        ScheduleSecHubJob job1 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job1.addData("other-key1", value);
        entityManager.persist(job1);

        ScheduleSecHubJob job2 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job2.addData("other-key1", value);
        job2.addData(key, value);
        entityManager.persist(job2);

        ScheduleSecHubJob job3 = jobCreator.being(ExecutionState.READY_TO_START).project(projectId).create();
        job3.addData("other-key2", value);
        job3.addData(key, value);
        entityManager.persist(job3);

        entityManager.flush();
        entityManager.clear();

        // search filter and specification
        Map<String, String> dataToSearchFor = new LinkedHashMap<>();
        dataToSearchFor.put(key, value);

        Specification<ScheduleSecHubJob> specification = ScheduleSecHubJobSpecifications.hasProjectIdAndData(projectId, dataToSearchFor);

        /* execute */
        List<ScheduleSecHubJob> results = jobRepository.findAll(specification);

        /* test */
        assertEquals(2, results.size());
        assertFalse(results.contains(job1));
        assertTrue(results.contains(job2));
        assertTrue(results.contains(job3));

    }

    @Test
    void create_with_data_stores_data() {
        /* prepare */
        String key = "testkey1";

        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();
        UUID jobUUID = newJob.getUUID();
        newJob.addData(key, "testvalue1");

        /* execute */
        entityManager.persist(newJob);

        /* test */
        entityManager.flush();
        entityManager.clear();

        // check data entity is persisted as well
        ScheduleSecHubJobData result = findDataOrNullByJobUUID(key, jobUUID);
        assertNotNull(result);

        Optional<ScheduleSecHubJob> found = jobRepository.findById(jobUUID);
        assertTrue(found.isPresent());
        ScheduleSecHubJob foundJob = found.get();
        assertEquals(1, foundJob.data.size());

        ScheduleSecHubJobData jobData1 = foundJob.data.iterator().next();
        assertEquals(jobUUID, jobData1.getJobUUID());
        assertEquals(key, jobData1.getId());
        assertEquals("testvalue1", jobData1.getValue());

    }

    @Test
    void delete_job_with_data_deletes_data() {
        /* prepare */
        String key = "testkey1";

        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();
        UUID jobUUID = newJob.getUUID();
        newJob.addData(key, "testvalue1");

        entityManager.persist(newJob);

        entityManager.flush();
        entityManager.clear();

        /* execute */
        jobRepository.deleteById(jobUUID);

        /* test */
        entityManager.flush();
        entityManager.clear();

        // check data entity is deleted
        ScheduleSecHubJobData result = findDataOrNullByJobUUID(key, jobUUID);
        assertNull(result);
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectNotYetExecuted_one_available() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(Set.of(0L));

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectNotYetExecuted_2_projects_1_project_running() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(Set.of(0L));

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob3.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_2_projects_1_project_running_all_same_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob3.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_2_projects_1_project_running_different_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.DYNAMIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob2.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_2_project_running_different_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.DYNAMIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob2.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_2_project_running_same_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob3.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_4_projects_no_project_started_all_ready() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p4").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob1.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_different_states() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.INITIALIZING).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob6 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));
        assertTrue(newJob5.created.isAfter(newJob4.created));
        assertTrue(newJob6.created.isAfter(newJob5.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob6.getUUID(), uuid.get());
    }

    @Test
    void custom_query_nextJobIdToExecuteFirstInFirstOut() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteFirstInFirstOut(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob.getUUID(), uuid.get());
    }

    @Test
    void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(2, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1_one_deleted_was_with_data() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* special : add data */
        ScheduleSecHubJob jobToUpdate = jobRepository.findById(testData.job1_90_days_before_created.uUID).get();
        jobToUpdate.addData("key1", "val1");
        jobToUpdate.addData("key2", "val1");
        jobRepository.save(jobToUpdate);
        jobRepository.flush();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        // check data entities are persisted as well
        ScheduleSecHubJobData data1a = entityManager.find(ScheduleSecHubJobData.class, new ScheduleSecHubJobDataId(jobToUpdate.getUUID(), "key1"));
        ScheduleSecHubJobData data2a = entityManager.find(ScheduleSecHubJobData.class, new ScheduleSecHubJobDataId(jobToUpdate.getUUID(), "key1"));
        assertNotNull(data1a);
        assertNotNull(data2a);

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);

        /* test */
        jobRepository.flush();
        entityManager.clear();

        assertDeleted(3, deleted, testData, olderThan);
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());

        /* @formatter:off
         *  Check data entities are not deleted by the former call - it is currently not possible
         *  for Hibernate to do a cascading delete by a query. So the test is more or less a canary to
         *  check if the situation is still the same (after library updates).
         *  ---> currently the SecHubJobDataRepository has its own deleteOlderThan method !
         *  @formatter:on
         */
        ScheduleSecHubJobData data1b = entityManager.find(ScheduleSecHubJobData.class, new ScheduleSecHubJobDataId(jobToUpdate.getUUID(), "key1"));
        ScheduleSecHubJobData data2b = entityManager.find(ScheduleSecHubJobData.class, new ScheduleSecHubJobDataId(jobToUpdate.getUUID(), "key1"));
        assertNotNull(data1b);
        assertNotNull(data2b);

    }

    @Test
    void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job1_90_days_before_created));
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(4, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_oldest_90_days_delete_90_days_deletes_0() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
    }

    @Test
    void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_oldest_90_days_deleted_1() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
    }

    @Test
    void findByUUID__the_job_is_returned_when_existing() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.create();

        /* execute */
        Optional<ScheduleSecHubJob> job = jobRepository.findById(newJob.getUUID());

        /* test */
        assertTrue(job.isPresent());
        assertEquals(newJob.getUUID(), job.get().getUUID());
    }

    @Test
    void findNextJobToExecute__and_no_jobs_available_at_all_null_is_returned_when_existing() {

        assertFalse(jobRepository.nextJobIdToExecuteFirstInFirstOut(FIRST_ENCRYPTION_POOL_ENTRY_ONLY).isPresent());
    }

    @Test
    void findNextJobToExecute__and_no_executable_job_available_at_all_null_is_returned_when_existing() {
        /* prepare */
        jobCreator.newJob().being(STARTED).create();

        /* execute + test */
        assertFalse(jobRepository.nextJobIdToExecuteFirstInFirstOut(FIRST_ENCRYPTION_POOL_ENTRY_ONLY).isPresent());

    }

    @Test
    void findNextJobToExecute__the_first_job_in_state_READY_TO_START_is_returned_when_existing() {
        /* prepare @formatter:off*/

        jobCreator.newJob().being(STARTED).createAnd().
                   newJob().being(CANCEL_REQUESTED).createAnd().
                   newJob().being(CANCELED).createAnd().
                   newJob().being(ENDED).create();

        ScheduleSecHubJob expectedNextJob =
        jobCreator.newJob().being(READY_TO_START).create();

        TestUtil.waitMilliseconds(1); // just enough time to make the next job "older" than former one, so we got no flaky tests when checking jobUUID later


        jobCreator.newJob().being(STARTED).createAnd().
                   newJob().being(READY_TO_START).create();

        /* execute */
        Optional<UUID> optional = jobRepository.nextJobIdToExecuteFirstInFirstOut(FIRST_ENCRYPTION_POOL_ENTRY_ONLY);
        assertTrue(optional.isPresent());

        UUID jobUUID = optional.get();

        /* test @formatter:on*/
        assertThat(jobUUID).isNotNull();
        assertEquals(expectedNextJob.getUUID(), jobUUID);
    }

    @ParameterizedTest
    @ArgumentsSource(NextJobIdToExecuteWithEncryptionPoolTestDataArgumentProvider.class)
    void nextJobIdToExecuteForProjectNotYetExecuted_2_projects_1_project_running(Set<Long> currentEncryptionPoolIds, TestResultVariant expectedVariant) {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3p0 = jobCreator.project("p2").encryptionPoolId(0L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3p1 = jobCreator.project("p2").encryptionPoolId(1L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3p2 = jobCreator.project("p2").encryptionPoolId(2L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3p3 = jobCreator.project("p2").encryptionPoolId(3L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3p0.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> optional = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(currentEncryptionPoolIds);
        if (expectedVariant == null || TestResultVariant.NONE.equals(expectedVariant)) {
            assertFalse(optional.isPresent());
            return;
        }
        assertTrue(optional.isPresent());

        UUID jobUUID = optional.get();

        /* test @formatter:on*/
        assertThat(jobUUID).isNotNull();
        switch (expectedVariant) {
        case POOL_ID_0:
            assertThat(jobUUID).isEqualTo(newJob3p0.getUUID());
            break;
        case POOL_ID_1:
            assertThat(jobUUID).isEqualTo(newJob3p1.getUUID());
            break;
        case POOL_ID_2:
            assertThat(jobUUID).isEqualTo(newJob3p2.getUUID());
            break;
        case POOL_ID_3:
            assertThat(jobUUID).isEqualTo(newJob3p3.getUUID());
            break;
        default:
            throw new IllegalStateException("not implemented for variant: " + expectedVariant);

        }
    }

    @ParameterizedTest
    @ArgumentsSource(NextJobIdToExecuteWithEncryptionPoolTestDataArgumentProvider.class)
    void nextJobIdToExecuteFirstInFirstOut__the_job_with_has_supported_encryption_in_state_READY_TO_START_multi_poolids(Set<Long> currentEncryptionPoolIds,
            TestResultVariant expectedVariant) {
        /* prepare @formatter:off*/

        jobCreator.newJob().being(STARTED).createAnd().
        newJob().being(CANCEL_REQUESTED).createAnd().
        newJob().being(CANCELED).createAnd().
        newJob().being(ENDED).create();
        ScheduleSecHubJob expectedNextJobWhenPoolId0Supported =
                jobCreator.newJob().encryptionPoolId(0L).being(READY_TO_START).create();
        ScheduleSecHubJob expectedNextJobWhenPoolId1Supported =
                jobCreator.newJob().encryptionPoolId(1L).being(READY_TO_START).create();
        ScheduleSecHubJob expectedNextJobWhenPoolId2Supported =
                jobCreator.newJob().being(READY_TO_START).encryptionPoolId(2L).create();
        ScheduleSecHubJob expectedNextJobWhenPoolId3Supported =
                jobCreator.newJob().being(READY_TO_START).encryptionPoolId(3L).create();

        TestUtil.waitMilliseconds(1); // just enough time to make the next job "older" than former one, so we got no flaky tests when checking jobUUID later


        jobCreator.newJob().being(STARTED).createAnd().
        newJob().being(READY_TO_START).create();

        /* execute */
        Optional<UUID> optional = jobRepository.nextJobIdToExecuteFirstInFirstOut(currentEncryptionPoolIds);
        if (expectedVariant==null || TestResultVariant.NONE.equals(expectedVariant)) {
            assertFalse(optional.isPresent());
            return;
        }
        assertTrue(optional.isPresent());

        UUID jobUUID = optional.get();

        /* test @formatter:on*/
        assertThat(jobUUID).isNotNull();
        switch (expectedVariant) {
        case POOL_ID_0:
            assertEquals(expectedNextJobWhenPoolId0Supported.getUUID(), jobUUID);
            break;
        case POOL_ID_1:
            assertEquals(expectedNextJobWhenPoolId1Supported.getUUID(), jobUUID);
            break;
        case POOL_ID_2:
            assertEquals(expectedNextJobWhenPoolId2Supported.getUUID(), jobUUID);
            break;
        case POOL_ID_3:
            assertEquals(expectedNextJobWhenPoolId3Supported.getUUID(), jobUUID);
            break;
        default:
            throw new IllegalStateException("not implemented for variant: " + expectedVariant);

        }
    }

    @ParameterizedTest
    @ArgumentsSource(NextJobIdToExecuteWithEncryptionPoolTestDataArgumentProvider.class)
    void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_in_state_READY_TO_START_multi_poolids(
            Set<Long> currentEncryptionPoolIds, TestResultVariant expectedVariant) {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.INITIALIZING).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();

        ScheduleSecHubJob newJob6v0 = jobCreator.project("p3").encryptionPoolId(0L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob6v1 = jobCreator.project("p3").encryptionPoolId(1L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob6v2 = jobCreator.project("p3").encryptionPoolId(2L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob6v3 = jobCreator.project("p3").encryptionPoolId(3L).module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));
        assertTrue(newJob5.created.isAfter(newJob4.created));
        assertTrue(newJob6v0.created.isAfter(newJob5.created));

        /* execute */
        Optional<UUID> optional = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(currentEncryptionPoolIds);

        /* test */
        if (expectedVariant == null || TestResultVariant.NONE.equals(expectedVariant)) {
            assertThat(optional).isNotPresent();
            return;
        }
        assertTrue(optional.isPresent());

        UUID jobUUID = optional.get();

        /* test @formatter:on*/
        assertThat(jobUUID).isNotNull();
        switch (expectedVariant) {
        case POOL_ID_0:
            assertEquals(newJob6v0.getUUID(), jobUUID);
            break;
        case POOL_ID_1:
            assertEquals(newJob6v1.getUUID(), jobUUID);
            break;
        case POOL_ID_2:
            assertEquals(newJob6v2.getUUID(), jobUUID);
            break;
        case POOL_ID_3:
            assertEquals(newJob6v3.getUUID(), jobUUID);
            break;
        default:
            throw new IllegalStateException("not implemented for variant: " + expectedVariant);

        }
    }

    private enum TestResultVariant {
        NONE, POOL_ID_0, POOL_ID_1, POOL_ID_2, POOL_ID_3;
    }

    static class NextJobIdToExecuteWithEncryptionPoolTestDataArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
            /* @formatter:off */
                        Arguments.of(Set.of(), TestResultVariant.NONE),
                        Arguments.of(Set.of(0), TestResultVariant.POOL_ID_0),
                        Arguments.of(Set.of(1), TestResultVariant.POOL_ID_1),
                        Arguments.of(Set.of(2), TestResultVariant.POOL_ID_2),
                        Arguments.of(Set.of(3), TestResultVariant.POOL_ID_3),
                        Arguments.of(Set.of(1,2), TestResultVariant.POOL_ID_1),
                        Arguments.of(Set.of(0,1,2,3), TestResultVariant.POOL_ID_0),
                        Arguments.of(Set.of(1,2,3), TestResultVariant.POOL_ID_1),
                        Arguments.of(Set.of(3,2,0), TestResultVariant.POOL_ID_0),
                        Arguments.of(Set.of(0,3),TestResultVariant.POOL_ID_0)
                    );
                   /* @formatter:on */

        }

    }

    private ScheduleSecHubJobData findDataOrNullByJobUUID(String key, UUID jobUUID) {
        return entityManager.find(ScheduleSecHubJobData.class, new ScheduleSecHubJobDataId(jobUUID, key));
    }

    private void createJobUsingEncryptionPoolId(long poolId, ExecutionState state) {
        jobCreator.project("p2").module(ModuleGroup.STATIC).being(state).encryptionPoolId(poolId).create();

    }

    private void assertDeleted(int expected, int deleted, DeleteJobTestData testData, LocalDateTime olderThan) {
        if (deleted == expected) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ScheduleSecHubJob> all = jobRepository.findAll();
        sb.append("Delete call did return ").append(deleted).append(" uploadMaximumBytes was ").append(expected).append("\n");
        sb.append("The remaining entries are:\n");
        for (ScheduleSecHubJob info : all) {
            sb.append(resolveName(info.created, testData)).append("- since       : ").append(info.created).append("\n");
        }
        sb.append("\n-----------------------------------------------------");
        sb.append("\nolderThan was: ").append(olderThan).append(" - means :").append((resolveName(olderThan, testData)));
        sb.append("\n-----------------------------------------------------\n");
        sb.append(describe(testData.job1_90_days_before_created, testData));
        sb.append(describe(testData.job2_2_days_before_created, testData));
        sb.append(describe(testData.job3_1_day_before_created, testData));
        sb.append(describe(testData.job4_now_created, testData));

        throw new AssertionError(sb.toString());
    }

    private String describe(ScheduleSecHubJob info, DeleteJobTestData data) {
        return resolveName(info.created, data) + " - created: " + info.created + "\n";
    }

    private String resolveName(LocalDateTime time, DeleteJobTestData data) {
        if (data.job1_90_days_before_created.created.equals(time)) {
            return "job1_90_days_before_created";
        }
        if (data.job2_2_days_before_created.created.equals(time)) {
            return "job2_2_days_before_created";
        }
        if (data.job3_1_day_before_created.created.equals(time)) {
            return "job3_1_day_before_created";
        }
        if (data.job4_now_created.created.equals(time)) {
            return "job4_now_created";
        }
        return null;
    }

    private class DeleteJobTestData {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before_89_days = now.minusDays(89);
        LocalDateTime before_90_days = now.minusDays(90);
        LocalDateTime before_3_days = now.minusDays(3);
        LocalDateTime before_1_day = now.minusDays(1);

        ScheduleSecHubJob job1_90_days_before_created;
        ScheduleSecHubJob job2_2_days_before_created;
        ScheduleSecHubJob job3_1_day_before_created;
        ScheduleSecHubJob job4_now_created;

        private void createAndCheckAvailable() {
            job1_90_days_before_created = jobCreator.created(before_90_days).started(null).create();
            job2_2_days_before_created = jobCreator.created(before_3_days).started(before_1_day).being(ExecutionState.STARTED).create();
            job3_1_day_before_created = jobCreator.created(before_1_day).started(now).create();
            job4_now_created = jobCreator.created(now).create();

            // check preconditions
            jobRepository.flush();
            assertEquals(4, jobRepository.count());
            List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
            assertTrue(allJobsNow.contains(job1_90_days_before_created));
            assertTrue(allJobsNow.contains(job2_2_days_before_created));
            assertTrue(allJobsNow.contains(job3_1_day_before_created));
            assertTrue(allJobsNow.contains(job4_now_created));
        }
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
