// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Map;

public interface AdapterConfig extends TrustAllConfig, TraceIdProvider{
    
    
	int getTimeOutInMilliseconds();

	/**
	 *
	 * @return time to wait , usable for {@link WaitForStateSupport}. Normally this describes time to wait for next operation
	 */
	int getTimeToWaitForNextCheckOperationInMilliseconds();

	/**
	 *
	 * @return base url as string, never <code>null</code>
	 */
	String getProductBaseURL();

	/**
	 *
	 * @return a base 64 encoded token containing "USERID:APITOKEN" inside
	 */
	String getCredentialsBase64Encoded();

	String getUser();

	String getPolicyId();

	String getPasswordOrAPIToken();

	/**
	 * Returns a map for options. Can be used to provide special behaviours which are not default. E.g. wire mock extensions etc.
	 * @return map with options
	 */
	public Map<AdapterOptionKey, String> getOptions();

	/**
	 * @return the project id or <code>null</code> if none set
	 */
	String getProjectId();

}