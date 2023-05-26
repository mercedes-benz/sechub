// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.log;

import static com.mercedesbenz.sechub.domain.scan.log.ProjectScanLog.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class ProjectScanLogRepositoryImpl implements ProjectScanLogRepositoryCustom {

    /* @formatter:off */
	public static final String JPQL_STRING_SELECT_BY_PROJECT_ID=
			"select new "+ProjectScanLogSummary.class.getName()+
			"("+
			   "l."+PROPERTY_SECHUB_JOB_UUID+","+
			   "l."+PROPERTY_EXECUTED_BY+","+
			   "l."+PROPERTY_STARTED+","+
			   "l."+PROPERTY_ENDED+","+
			   "l."+PROPERTY_STATUS+
			") from "+CLASS_NAME+" l"+
			" where l."+PROPERTY_PROJECT_ID+" = :"+PROPERTY_PROJECT_ID +
			" order by l."+PROPERTY_STARTED+" DESC";
	/* @formatter:on */

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Override
    public List<ProjectScanLogSummary> findSummaryLogsFor(String projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException();
        }
        Query query = em.createQuery(JPQL_STRING_SELECT_BY_PROJECT_ID);
        query.setParameter(PROPERTY_PROJECT_ID, projectId);

        return query.getResultList();
    }
}
