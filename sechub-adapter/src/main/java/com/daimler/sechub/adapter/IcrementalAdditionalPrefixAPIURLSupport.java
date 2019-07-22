// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Map;

import com.daimler.sechub.adapter.support.APIURLSupport;

/**
 * A special api url support which does always add an additonal prefix with
 * automated incrementation. Interesting for wiremock tests to provide always different
 * urls, so this class can be used as a workaround when testing adapters with wiremock (wire mock is not able to handle GET-Requests with request bodies,
 * also the "scenario"-aproach is sometimes a little bit cumbersome when having multiple calls).  By using this support you can easily and exactly check the correct
 * ordering and call hierarchy.
 * 
 * <pre>
    incrementalSupport = new IcrementalAdditionalPrefixAPIURLSupport("teststep");
    
    // lets use 
    adapterToTest = new XyzAdapter(){
    	createAPIURLSupport(){
    		return incrementalSupport;
    	}
    }
    
    stubFor(post(urlEqualTo(incrementalSupport.nextURL("/api/1.0/person"))...
    ...
    adapterToTest.executeAction()...
    
    ...
    verify(postRequestedFor(urlEqualTo("/teststep_1/api/1.0/person"))); // checks teh first rest call had was the post
 * </pre>
 * 
 * @author Albert Tregnaghi
 *
 */
public class IcrementalAdditionalPrefixAPIURLSupport extends APIURLSupport {
	private static final int START_INDEX = 1;// we start with 1
	private int createdPrefixIndex = START_INDEX;
	private int checkingPrefixIndex = START_INDEX;
	private String additionalPrefix;

	public IcrementalAdditionalPrefixAPIURLSupport(String additionalPrefix) {
		this.additionalPrefix = additionalPrefix;
	}

	@Override
	public String createAPIURL(String apiPath, AdapterConfig config, String apiPrefix, String otherBaseURL, Map<String,String> map) {
		String newPrefix = getPrefix(createdPrefixIndex++);
		if (apiPrefix != null) {
			newPrefix += "/" + apiPrefix;
		}
		return super.createAPIURL(apiPath, config, newPrefix, otherBaseURL,map);
	}

	private String getPrefix(int value) {
		return additionalPrefix + "_" + value;
	}

	private String nextCheckPrefix() {
		return getPrefix(checkingPrefixIndex++);
	}

	/**
	 * @param url
	 * @return next URL to check
	 */
	public String nextURL(String url) {
		return "/" + nextCheckPrefix() + url;
	}

	/**
	 * Assert current checked position is as expected. This is normally
	 * a checked marker in code for easier debugging. So you are sure which 
	 * index is used for a test / stubbing.
	 * 
	 * @param expectedCheckIndex
	 * @return itself
	 */
	public IcrementalAdditionalPrefixAPIURLSupport assertCheck(int expectedCheckIndex) {
		if (expectedCheckIndex != checkingPrefixIndex) {
			throw new IllegalStateException(
					"Expected position was:" + expectedCheckIndex + ", but was:" + checkingPrefixIndex);
		}
		return this;
	}

}