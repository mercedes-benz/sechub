// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.sharedkernel.jpa.TypedQuerySupport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class SecHubJobRepositoryImpl implements SecHubJobRepositoryCustom {

    private static final String PARAM_UUID = "p_uuid";
    private static final String PARAM_EXECUTION_STATE = "p_exec_state";
    private static final String PARAM_EXECUTION_STATE_SUB = "p_sub_exec_state";
    private static final String PARAM_ENCRYPTION_POOL_ID = "p_encrypt_pool_id";

    /* @formatter:off */
	static final String JPQL_STRING_SELECT_BY_EXECUTION_STATE =
			"select j from " + CLASS_NAME + " j" +
			" where j." + PROPERTY_EXECUTION_STATE + " = :" + PARAM_EXECUTION_STATE +
			" order by j." + PROPERTY_CREATED;

	static final String JPQL_STRING_SELECT_BY_JOB_ID =
            "select j from " + CLASS_NAME + " j" +
            " where j." + PROPERTY_EXECUTION_STATE + " = :" + PARAM_EXECUTION_STATE +
            " and j." + PROPERTY_UUID + " = :" + PARAM_UUID;

	static final String SUB_JPQL_STRING_SELECT_PROJECTS_WITH_RUNNING_JOBS =
	        "select p. " + PROPERTY_PROJECT_ID + " from " + CLASS_NAME + " p" +
	                " where p." + PROPERTY_EXECUTION_STATE + " = " + ":"+PARAM_EXECUTION_STATE_SUB;

	static final String JPQL_STRING_SELECT_JOB_WHERE_NOT_YET_RUNNING_SAME_PROJECT =
            "select j from " + CLASS_NAME + " j" +
            " where j." + PROPERTY_EXECUTION_STATE + " = :" + PARAM_EXECUTION_STATE +
            " and j." + PROPERTY_PROJECT_ID +
            " not in ( " + SUB_JPQL_STRING_SELECT_PROJECTS_WITH_RUNNING_JOBS + " )" +
            " order by " + PROPERTY_CREATED;

    static final String SUB_JPQL_STRING_SELECT_PROJECTS_WITH_RUNNING_JOBS_AND_SAME_MODULE_GROUP =
            "select p. " + PROPERTY_PROJECT_ID + " from " + CLASS_NAME + " p" +
                    " where p." + PROPERTY_EXECUTION_STATE + " = " + ":"+PARAM_EXECUTION_STATE_SUB +
                    " and p."+PROPERTY_MODULE_GROUP+" = j."+PROPERTY_MODULE_GROUP;


	static final String JPQL_STRING_SELECT_JOB_WHERE_NOT_YET_RUNNING_SAME_PROJECT_IN_SAME_GROUP =
	        "select j from " + CLASS_NAME + " j" +
	                " where j." + PROPERTY_EXECUTION_STATE + " = :" + PARAM_EXECUTION_STATE +
	                " and j." + PROPERTY_PROJECT_ID +
	                " not in ( " + SUB_JPQL_STRING_SELECT_PROJECTS_WITH_RUNNING_JOBS_AND_SAME_MODULE_GROUP + " )" +
	                " order by " + PROPERTY_CREATED;


    static final String JPQL_STRING_SELECT_RANDOM_JOB_CANCELED_OR_ENDED_WHERE_POOL_ID_IS_SMALLER_THAN_GIVEN_ONE =
            "select j from " + CLASS_NAME + " j" +
                    " where (j." + PROPERTY_EXECUTION_STATE + " = " + ExecutionState.ENDED + " or j." + PROPERTY_EXECUTION_STATE + " = " +ExecutionState.CANCELED +")"+
                    " and j." + PROPERTY_ENCRYPTION_POOL_ID + " < :" + PARAM_ENCRYPTION_POOL_ID +
                    " order by random()";


    /* @formatter:on */

    private final TypedQuerySupport<ScheduleSecHubJob> typedQuerySupport = new TypedQuerySupport<>(ScheduleSecHubJob.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ScheduleSecHubJob> getJob(UUID id) {
        Query query = em.createQuery(JPQL_STRING_SELECT_BY_JOB_ID);
        query.setParameter(PARAM_EXECUTION_STATE, ExecutionState.READY_TO_START);
        query.setParameter(PARAM_UUID, id);
        query.setMaxResults(1);
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return typedQuerySupport.getSingleResultAsOptional(query);
    }

    @Override
    public Optional<UUID> nextJobIdToExecuteFirstInFirstOut() {
        Query query = em.createQuery(JPQL_STRING_SELECT_BY_EXECUTION_STATE);
        query.setParameter(PARAM_EXECUTION_STATE, ExecutionState.READY_TO_START);
        query.setMaxResults(1);
        // we use OPTIMISTIC_FORCE_INCREMENT write lock - so only one POD will be able
        // to execute next job...
        // see https://www.baeldung.com/jpa-pessimistic-locking
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return getUUIDFromJob(typedQuerySupport.getSingleResultAsOptional(query));
    }

    @Override
    public Optional<UUID> nextJobIdToExecuteForProjectNotYetExecuted() {
        Query query = em.createQuery(JPQL_STRING_SELECT_JOB_WHERE_NOT_YET_RUNNING_SAME_PROJECT);
        query.setParameter(PARAM_EXECUTION_STATE, ExecutionState.READY_TO_START);
        query.setParameter(PARAM_EXECUTION_STATE_SUB, ExecutionState.STARTED);
        query.setMaxResults(1);
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return getUUIDFromJob(typedQuerySupport.getSingleResultAsOptional(query));
    }

    private Optional<UUID> getUUIDFromJob(Optional<ScheduleSecHubJob> job) {
        if (job.isPresent() && job.get().getUUID() != null) {
            return Optional.of(job.get().getUUID());
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted() {
        Query query = em.createQuery(JPQL_STRING_SELECT_JOB_WHERE_NOT_YET_RUNNING_SAME_PROJECT_IN_SAME_GROUP);
        query.setParameter(PARAM_EXECUTION_STATE, ExecutionState.READY_TO_START);
        query.setParameter(PARAM_EXECUTION_STATE_SUB, ExecutionState.STARTED);
        query.setMaxResults(1);
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return getUUIDFromJob(typedQuerySupport.getSingleResultAsOptional(query));
    }

    @Override
    public List<ScheduleSecHubJob> nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(Long encryptionPoolId, int maxAmount) {
        Query query = em.createQuery(JPQL_STRING_SELECT_RANDOM_JOB_CANCELED_OR_ENDED_WHERE_POOL_ID_IS_SMALLER_THAN_GIVEN_ONE);
        query.setParameter(PARAM_ENCRYPTION_POOL_ID, encryptionPoolId);
        query.setMaxResults(maxAmount);
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return typedQuerySupport.getList(query);
    }

}
