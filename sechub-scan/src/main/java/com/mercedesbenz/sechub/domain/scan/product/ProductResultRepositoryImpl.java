// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static com.mercedesbenz.sechub.domain.scan.product.ProductResult.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class ProductResultRepositoryImpl implements ProductResultRepositoryCustom {
    /* @formatter:off */
	public static final String JPQL_STRING_SELECT_BY_SECHUB_JOB_UUID_AND_PRODUCT_IDS =
			"select r from "+CLASS_NAME+" r"+
					" where r."+PROPERTY_SECHUB_JOB_UUID+" = :"+PROPERTY_SECHUB_JOB_UUID +
					" and r."+PROPERTY_PRODUCT_IDENTIFIER+" in :"+PROPERTY_PRODUCT_IDENTIFIER;

	public static final String JPQL_STRING_SELECT_BY_SECHUB_JOB_UUID_AND_PRODUCT_CONFIG_UUID =
	        "select r from "+CLASS_NAME+" r"+
	                " where r."+PROPERTY_SECHUB_JOB_UUID+" = :"+PROPERTY_SECHUB_JOB_UUID +
	                " and r."+PROPERTY_PRODUCT_CONFIG_UUID+" = :"+PROPERTY_PRODUCT_CONFIG_UUID;
	/* @formatter:on */

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Override
    public List<ProductResult> findProductResults(UUID secHubJobUUID, ProductExecutorConfigInfo info) {
        if (secHubJobUUID == null) {
            throw new IllegalArgumentException("secHubJobUUID may not be null!");
        }
        if (info == null) {
            throw new IllegalArgumentException("productExecutorConfiguration may not be null!");
        }
        UUID configUUID = info.getUUID();
        if (configUUID == null) {
            throw new IllegalArgumentException("configUUID may not be null!");
        }

        Query query = em.createQuery(JPQL_STRING_SELECT_BY_SECHUB_JOB_UUID_AND_PRODUCT_CONFIG_UUID);
        query.setParameter(PROPERTY_SECHUB_JOB_UUID, secHubJobUUID);
        query.setParameter(PROPERTY_PRODUCT_CONFIG_UUID, configUUID);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProductResult> findAllProductResults(UUID secHubJobUUID, ProductIdentifier... allowedIdentifiers) {
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
