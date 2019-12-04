// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static com.daimler.sechub.sharedkernel.UUIDTraceLogID.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

/**
 * Abstract base implementation for all product execution services. Service will
 * execute registered executors and persist the results automatic
 *
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractProductExecutionService implements ProductExectionStoreService {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractProductExecutionService.class);

	private List<ProductExecutor> productExecutors = new ArrayList<>();

	@Autowired
	ProductResultRepository productResultRepository;

	/**
	 * Registers given product executors which shall be executed
	 *
	 * @param productExecutors
	 */
	protected void register(List<? extends ProductExecutor> productExecutors) {
		this.productExecutors.addAll(productExecutors);
	}

	/**
	 * Executes product executors and stores results. If a result of an executor is
	 * <code>null</code> an error will be logged but
	 *
	 * @param context
	 * @throws SecHubExecutionException
	 */
	public void executeProductsAndStoreResults(SecHubExecutionContext context) throws SecHubExecutionException {
		try {

			UUIDTraceLogID traceLogID = traceLogID(context.getSechubJobUUID());

			SecHubConfiguration configuration = context.getConfiguration();
			if (!isExecutionNecessary(context, traceLogID, configuration)) {
				LOG.debug("NO execution necessary by {}", getClass().getSimpleName());
				return;
			}
			executeAndPersistResults(productExecutors, context, traceLogID);
		} catch (RuntimeException e) {
			/* catch runtime errors and move and wrapt in SecHubExecutionException */
			throw new SecHubExecutionException("Product execution + store failed unexpected", e);
		}
	}

	protected abstract boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration);

	protected List<ProductResult> execute(ProductExecutor executor, SecHubExecutionContext context, UUIDTraceLogID traceLogID) throws SecHubExecutionException {

		LOG.info("Start executor {} and wait for result. {}", executor.getIdentifier(), traceLogID);

		List<ProductResult> productResults = executor.execute(context);
		int amount = 0;
		if (productResults != null) {
			amount = productResults.size();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Product '{}' returned for {} results:\n{}", executor.getIdentifier(), traceLogID, amount);
		}
		if (LOG.isTraceEnabled()) {
			if (amount > 0) {
				int pos = 1;

				for (ProductResult result : productResults) {
					LOG.trace("Product '{}' returned for {} result {}/{}:\n{}", executor.getIdentifier(), traceLogID, pos++, amount, result);
				}
			}
		}
		return productResults;

	}

	/**
	 * Executes product executors and stores results. If a result of an executor is
	 * <code>null</code> an error will be logged but
	 *
	 * @param executors
	 * @param context
	 * @param traceLogID
	 */
	protected void executeAndPersistResults(List<? extends ProductExecutor> executors, SecHubExecutionContext context, UUIDTraceLogID traceLogID) {
		SecHubConfiguration configuration = context.getConfiguration();
		requireNonNull(configuration,"Configuration must be set");

		String projectId = configuration.getProjectId();
		requireNonNull(projectId, "Project id must be set");

		for (ProductExecutor productExecutor : executors) {
			List<ProductResult> productResults=Collections.emptyList();
			try {
				productResults = execute(productExecutor, context, traceLogID);
				if (productResults == null) {
					getMockableLog().error("Product executor {} returned null as results {}", productExecutor.getIdentifier(), traceLogID);
					continue;
				}
			} catch (Exception e) {
				getMockableLog().error("Product executor failed:"+productExecutor.getIdentifier()+" "+traceLogID,e);

				productResults=new ArrayList<ProductResult>();
				ProductResult fallbackResult = new ProductResult(context.getSechubJobUUID(),projectId, productExecutor.getIdentifier(),"");
				productResults.add(fallbackResult);
			}

			/* execution was successful */
			for (ProductResult productResult : productResults) {
				persistResult(traceLogID, productExecutor, productResult);
			}
		}
	}

	/**
	 * Persists the result. This will ALWAYS start a new transaction. So former
	 * results will NOT get lost if this persistence fails. Necessary for debugging
	 * and also the later possibility to relaunch already existing sechub jobs!
	 * Reason: When a former scan did take a very long time and was done. The next
	 * time another product exeuction fails because of problems inside the security
	 * infrastructure we do not want to restart all parts again, but only the failed
	 * / missing ones...<br>
	 * <br>
	 *
	 * @see https://www.ibm.com/developerworks/java/library/j-ts1/index.html for
	 *      details on REQUIRES_NEW when using ORM frameworks
	 * @param traceLogID
	 * @param productExecutor
	 * @param productResult
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistResult(UUIDTraceLogID traceLogID, ProductExecutor productExecutor, ProductResult productResult) {
		if (productResult == null) {
			getMockableLog().error("Product executor {} returned null as one of the results {}", productExecutor.getIdentifier(), traceLogID);
			return;
		}
		productResultRepository.save(productResult);
	}

	/**
	 * Normally unnecessary, but we want the ability to check log usage in tests
	 *
	 * @return log
	 */
	Logger getMockableLog() {
		return LOG;
	}
}
