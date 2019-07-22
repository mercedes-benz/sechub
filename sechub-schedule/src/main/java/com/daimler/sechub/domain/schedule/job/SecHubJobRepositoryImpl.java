// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static com.daimler.sechub.domain.schedule.ExecutionState.*;
import static com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.daimler.sechub.sharedkernel.jpa.TypedQuerySupport;

public class SecHubJobRepositoryImpl implements SecHubJobRepositoryCustom {
	/* @formatter:off */
	public static final String JPQL_STRING_SELECT_BY_EXECUTION_STATE = 
			"select j from "+CLASS_NAME+" j"+
					" where j."+PROPERTY_EXECUTION_STATE+" = :"+PROPERTY_EXECUTION_STATE +
					" order by j."+PROPERTY_CREATED;
	/* @formatter:on */

	private final TypedQuerySupport<ScheduleSecHubJob> typedQuerySupport = new TypedQuerySupport<>(ScheduleSecHubJob.class);

	@PersistenceContext
	private EntityManager em;

	@Override
	public Optional<ScheduleSecHubJob> findNextJobToExecute() {

		Query query = em.createQuery(JPQL_STRING_SELECT_BY_EXECUTION_STATE);
		query.setParameter(PROPERTY_EXECUTION_STATE, READY_TO_START);
		query.setMaxResults(1);
		// we use OPTIMISTIC_FORCE_INCREMENT write lock - so only one POD will be able to execute next job...
		// see https://www.baeldung.com/jpa-pessimistic-locking
		query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

		return typedQuerySupport.getSingleResultAsOptional(query);
	}

}
