// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static com.daimler.sechub.domain.schedule.ExecutionState.*;
import static com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.daimler.sechub.sharedkernel.jpa.TypedQuerySupport;

public class SecHubJobRepositoryImpl implements SecHubJobRepositoryCustom {
    /* @formatter:off */
	static final String JPQL_STRING_SELECT_BY_EXECUTION_STATE = 
			"select j from " + CLASS_NAME + " j" +
			" where j." + PROPERTY_EXECUTION_STATE + " = :" + PROPERTY_EXECUTION_STATE +
			" order by j." + PROPERTY_CREATED;
	
	static final String JPQL_STRING_SELECT_BY_JOB_ID = 
            "select j from " + CLASS_NAME + " j" +
            " where j." + PROPERTY_EXECUTION_STATE + " = :" + PROPERTY_EXECUTION_STATE +
            " and j." + PROPERTY_UUID + " = :" + PROPERTY_UUID;
    
	
	static final String SUB_JPQL_STRING_SELECT_PROJECTS_WITH_RUNNING_JOBS = 
	        "select p. " + PROPERTY_PROJECT_ID + " from " + CLASS_NAME + " p" +
	        " where p." + PROPERTY_EXECUTION_STATE + " = " + ":started_param";
	
	static final String JPQL_STRING_SELECT_JOB_WHERE_NOT_YET_RUNNING_SAME_PROJECT = 
            "select j from " + CLASS_NAME + " j" +
            " where j." + PROPERTY_EXECUTION_STATE + " = :" + PROPERTY_EXECUTION_STATE +
            " and j." + PROPERTY_PROJECT_ID + 
            " not in ( " + SUB_JPQL_STRING_SELECT_PROJECTS_WITH_RUNNING_JOBS + " )" +
            " order by " + PROPERTY_CREATED;
	
	
    /* @formatter:on */

    private final TypedQuerySupport<ScheduleSecHubJob> typedQuerySupport = new TypedQuerySupport<>(ScheduleSecHubJob.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ScheduleSecHubJob> findNextJobToExecute() {

        Query query = em.createQuery(JPQL_STRING_SELECT_BY_EXECUTION_STATE);
        query.setParameter(PROPERTY_EXECUTION_STATE, READY_TO_START);
        query.setMaxResults(1);
        // we use OPTIMISTIC_FORCE_INCREMENT write lock - so only one POD will be able
        // to execute next job...
        // see https://www.baeldung.com/jpa-pessimistic-locking
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return typedQuerySupport.getSingleResultAsOptional(query);
    }

    @Override
    public Optional<ScheduleSecHubJob> getJob(UUID id) {
        Query query = em.createQuery(JPQL_STRING_SELECT_BY_JOB_ID);
        query.setParameter(PROPERTY_EXECUTION_STATE, READY_TO_START);
        query.setParameter(PROPERTY_UUID, id);
        query.setMaxResults(1);
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        
        return typedQuerySupport.getSingleResultAsOptional(query);
    }

    @Override
    public Optional<UUID> nextJobIdToExecuteFirstInFirstOut() {        
        try {
            return getUUIDFromJob(findNextJobToExecute());
        }
        catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<UUID> nextJobIdToExecuteForProjectNotYetExecuted() {
        Query query = em.createQuery(JPQL_STRING_SELECT_JOB_WHERE_NOT_YET_RUNNING_SAME_PROJECT);
        query.setParameter(PROPERTY_EXECUTION_STATE, READY_TO_START);
        query.setParameter("started_param", STARTED);
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

}
