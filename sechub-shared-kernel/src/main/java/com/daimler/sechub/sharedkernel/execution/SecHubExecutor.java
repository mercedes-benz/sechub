// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.execution;

/**
 * Execute something for sechub
 * @author Albert Tregnaghi
 *
 * @param <T>
 */
public interface SecHubExecutor<T> {

	/**
	 * Execute within SecHub execution context
	 * 
	 * @param context
	 * @return result, or <code>null</code> when execution was not possible
	 * @throws SecHubExecutionException
	 *             when any problems occuring
	 */
	public T execute(SecHubExecutionContext context) throws SecHubExecutionException;
}
