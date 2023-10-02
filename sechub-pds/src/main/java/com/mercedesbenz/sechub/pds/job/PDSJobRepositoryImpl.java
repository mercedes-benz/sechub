// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState.*;
import static com.mercedesbenz.sechub.pds.job.PDSJob.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class PDSJobRepositoryImpl implements PDSJobRepositoryCustom {

    /* @formatter:off */
	public static final String JPQL_STRING_SELECT_BY_EXECUTION_STATE =
			"select j from "+CLASS_NAME+" j"+
					" where j."+PROPERTY_STATE+" = :"+PROPERTY_STATE +
					" and j."+PROPERTY_SERVER_ID+" = :"+PROPERTY_SERVER_ID +
					" order by j."+PROPERTY_CREATED;
	/* @formatter:on */

    /* @formatter:off */
    public static final String JPQL_STRING_SELECT_COUNT_BY_STATUS_AND_SERVER_ID =
            "select count(j) from "+CLASS_NAME+" j"+
                    " where j."+PROPERTY_SERVER_ID+" = :"+PROPERTY_SERVER_ID +
                    " and j."+PROPERTY_STATE+" = :"+PROPERTY_STATE ;
    /* @formatter:on */

    @PersistenceContext
    private EntityManager em;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Override
    public Optional<PDSJob> findNextJobToExecute() {
        Query query = em.createQuery(JPQL_STRING_SELECT_BY_EXECUTION_STATE);
        query.setParameter(PROPERTY_SERVER_ID, serverConfigService.getServerId());
        query.setParameter(PROPERTY_STATE, READY_TO_START);
        query.setMaxResults(1);
        // we use OPTIMISTIC_FORCE_INCREMENT write lock - so only one POD will be able
        // to execute next job... (it's just a fuse, it should not happen)
        // see https://www.baeldung.com/jpa-pessimistic-locking
        query.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        List<?> list = query.getResultList();
        Object singleResult = null;
        if (!list.isEmpty()) {
            singleResult = list.iterator().next();
        }
        return Optional.ofNullable((PDSJob) singleResult);
    }

    public long countJobsOfServerInState(String serverId, PDSJobStatusState state) {
        Query query = em.createQuery(JPQL_STRING_SELECT_COUNT_BY_STATUS_AND_SERVER_ID);
        query.setParameter(PROPERTY_SERVER_ID, serverConfigService.getServerId());
        query.setParameter(PROPERTY_STATE, state);
        query.setMaxResults(1);

        List<?> list = query.getResultList();
        Object singleResult = null;
        if (!list.isEmpty()) {
            singleResult = list.iterator().next();
        }
        Long result = (Long) singleResult;
        return result;
    }

}
