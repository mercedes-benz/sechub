// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;

import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutor;

/**
 * 
 * @author Albert Tregnaghi
 *
 */
public interface ProductExecutor extends ProductIdentifiable, SecHubExecutor<List<ProductResult>, ProductExecutorContext> {

    /**
     * Execute within SecHub execution context. <br>
     * <br>
     * Why is there a list as an result here? Because product can be executed in
     * different target situations we have a list of product results here (this is
     * only interesting when sechub execution does one scan for multiple
     * installation targets - e.g. when a scan configuration scans intranet and also
     * an internet location we got two results. Normally this does not happen, but
     * to handle even such strange situations the results are returned as a list)
     * 
     * @param context
     * @return a list of results, never <code>null</code>
     * @throws SecHubExecutionException when any problems occuring
     */
    public List<ProductResult> execute(SecHubExecutionContext context, ProductExecutorContext executorContext) throws SecHubExecutionException;

    /**
     * @return version of executor
     */
    public int getVersion();

    /**
     * Determines if it is okay to deefine multiple configurations of same executor. Default is false
     * @return <code>true</code> when it is allowed to install multiple executors
     *         for same {@link ProductExecutor#getIdentifier()}, <code>false</code>
     *         when only executor configuration is allowed for the product
     */
    public default boolean isMultipleConfigurationAllowed() {
        return false;
    }

}
