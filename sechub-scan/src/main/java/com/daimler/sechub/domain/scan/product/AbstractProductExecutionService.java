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

import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.report.ScanReportProductExecutor;
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

    private static final ProductExecutorConfig SERECO_FALLBACK = createFallbackExecutorConfigForSereco();

    @Autowired
    ProductResultRepository productResultRepository;

    @Autowired
    ProductExecutorConfigRepository productExecutorConfigRepository;

    @Autowired
    ProductExecutorContextFactory productExecutorContextFactory;

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
            if (context.isCanceledOrAbandonded()) {
                LOG.debug("{} canceled or abandoned, so ignored by {}", traceLogID, getClass().getSimpleName());
                return;
            }
            if (!isExecutionNecessary(context, traceLogID, configuration)) {
                LOG.debug("{} NO execution necessary by {}", traceLogID, getClass().getSimpleName());
                return;
            }
            runOnAllAvailableExecutors(getProductExecutors(), context, traceLogID);
        } catch (RuntimeException e) {
            /* catch runtime errors and move and wrapt in SecHubExecutionException */
            throw new SecHubExecutionException("Product execution + store failed unexpected", e);
        }
    }

    private static ProductExecutorConfig createFallbackExecutorConfigForSereco() {
        ProductExecutorConfigSetup setup = new ProductExecutorConfigSetup();
        ProductExecutorConfig executorConfiguration = new ProductExecutorConfig(ProductIdentifier.SERECO, 1, setup);
        executorConfiguration.getSetup().setBaseURL("embedded");
        return executorConfiguration;
    }

    protected abstract List<? extends ProductExecutor> getProductExecutors();

    protected abstract boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration);

    protected List<ProductResult> execute(ProductExecutor executor, ProductExecutorContext executorContext, SecHubExecutionContext context,
            UUIDTraceLogID traceLogID) throws SecHubExecutionException {

        LOG.info("Start executor {} and wait for result. {}", executor.getIdentifier(), traceLogID);

        List<ProductResult> productResults = executor.execute(context, executorContext);
        if (context.isCanceledOrAbandonded()) {
            return Collections.emptyList();
        }
        int amount = 0;
        if (productResults != null) {
            amount = productResults.size();
        }
        if (LOG.isTraceEnabled()) {
            int pos = 1;

            for (ProductResult result : productResults) {
                LOG.trace("Product '{}' returned for {} result {}/{}:\n{}", executor.getIdentifier(), traceLogID, pos++, amount, result);
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
    protected void runOnAllAvailableExecutors(List<? extends ProductExecutor> executors, SecHubExecutionContext context, UUIDTraceLogID traceLogID) {
        SecHubConfiguration configuration = context.getConfiguration();
        requireNonNull(configuration, "Configuration must be set");

        String projectId = configuration.getProjectId();
        requireNonNull(projectId, "Project id must be set");


        int countOfReports = 0;
        ProductExecutor serecoProductExecutor = null;
        
        for (ProductExecutor productExecutor : executors) {
            if (context.isCanceledOrAbandonded()) {
                return;
            }
            ProductIdentifier productIdentifier = productExecutor.getIdentifier();
            int executorVersion = productExecutor.getVersion();
            
            switch (productIdentifier) {

            case SERECO:
                serecoProductExecutor=productExecutor;
                /* fall through */
            default:
                LOG.debug("search config for project={}, executor={}, version={}", projectId, productIdentifier, executorVersion);
                List<ProductExecutorConfig> executorConfigurations = productExecutorConfigRepository.findExecutableConfigurationsForProject(projectId,
                        productIdentifier, executorVersion);
                if (executorConfigurations.isEmpty()) {
                    LOG.debug("no config found for project={} so skipping executor={}, version={}", projectId, productIdentifier,
                            executorVersion);
                    continue;
                }
                for (ProductExecutorConfig executorConfiguration : executorConfigurations) {
                    runOnExecutorWithOneConfiguration(executorConfiguration, productExecutor, context, projectId, traceLogID);
                    if (productExecutor instanceof ScanReportProductExecutor) {
                        countOfReports++;
                    }
                }

            }
        }
        
        if (serecoProductExecutor!=null && countOfReports==0) {
            LOG.debug("no dedicated configuration for report execution was executed before, so fallback to sereco default behaviour");
            runOnExecutorWithOneConfiguration(SERECO_FALLBACK, serecoProductExecutor, context, projectId, traceLogID);
        }
        
    }

    private void runOnExecutorWithOneConfiguration(ProductExecutorConfig executorConfiguration, ProductExecutor productExecutor, SecHubExecutionContext context,
            String projectId, UUIDTraceLogID traceLogID) {
        /*
         * find former results - necessary for restart, contains necessary meta data for
         * restart
         */
        List<ProductResult> formerResults = productResultRepository.findProductResults(context.getSechubJobUUID(), productExecutor.getIdentifier());
        ProductExecutorContext executorContext = productExecutorContextFactory.create(formerResults, context, productExecutor, executorConfiguration);

        List<ProductResult> productResults = null;
        try {
            productResults = execute(productExecutor, executorContext, context, traceLogID);
            if (context.isCanceledOrAbandonded()) {
                return;
            }
            if (productResults == null) {
                getMockableLog().error("Product executor {} returned null as results {}", productExecutor.getIdentifier(), traceLogID);
                return;
            }
        } catch (Exception e) {
            getMockableLog().error("Product executor failed:{} {}", productExecutor.getIdentifier(), traceLogID, e);

            productResults = new ArrayList<ProductResult>();
            ProductResult fallbackResult = new ProductResult(context.getSechubJobUUID(), projectId, productExecutor.getIdentifier(), "");
            productResults.add(fallbackResult);
        }
        if (context.isCanceledOrAbandonded()) {
            return;
        }
        /* execution was successful - so persist new results */
        for (ProductResult productResult : productResults) {
            executorContext.persist(productResult);
        }

        /* we drop former results which are duplicates */
        for (ProductResult oldResult : formerResults) {
            if (productResults.contains(oldResult)) {
                /* reused - so ignore */
                continue;
            }
            productResultRepository.delete(oldResult);
        }
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
