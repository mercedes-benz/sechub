// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static com.daimler.sechub.domain.scan.product.ProductResult.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class ProductResultRepositoryImpl implements ProductResultRepositoryCustom {
	/* @formatter:off */
	public static final String JPQL_STRING_SELECT_BY_SECHUB_JOB_UUID_AND_PRODUCT_IDS =
			"select r from "+CLASS_NAME+" r"+
					" where r."+PROPERTY_SECHUB_JOB_UUID+" = :"+PROPERTY_SECHUB_JOB_UUID +
					" and r."+PROPERTY_PRODUCT_IDENTIFIER+" in :"+PROPERTY_PRODUCT_IDENTIFIER;
	/* @formatter:on */

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<ProductResult> findProductResults(UUID secHubJobUUID, ProductIdentifier... allowedIdentifiers) {
		if (secHubJobUUID == null) {
			throw new IllegalArgumentException();
		}
		if (allowedIdentifiers == null || allowedIdentifiers.length == 0) {
			/* a shortcut - then no result is possible... */
			return new ArrayList<>();
		}
		Query query = em.createQuery(JPQL_STRING_SELECT_BY_SECHUB_JOB_UUID_AND_PRODUCT_IDS);
		query.setParameter(PROPERTY_SECHUB_JOB_UUID, secHubJobUUID);
		query.setParameter(PROPERTY_PRODUCT_IDENTIFIER, Arrays.asList(allowedIdentifiers));

		return query.getResultList();
	}

}
