// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.net.URI;
import java.net.URISyntaxException;

import com.daimler.sechub.test.ExampleConstants;

/**
 * See <code>mockdata_setup.json</code> for configuration parts
 * @author Albert Tregnaghi
 *
 */
public enum IntegrationTestMockMode {

	/**
	 * No waits for executed mocks.
	 */
	WEBSCAN__NETSPARKER_RESULT_GREEN__FAST("https://netsparker.green.demo." + ExampleConstants.URI_TARGET_SERVER),

	/**
	 * Web and infra scans will have 10 seconds elapse time on mock execution.
	 */
	WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING("https://netsparker.longrunning.but.green.demo." + ExampleConstants.URI_TARGET_SERVER),

	WEBSCAN__NETSPARKER_RESULT_ONE_FINDING__FAST("https://netsparker.vulnerable.demo." + ExampleConstants.URI_TARGET_SERVER),

	WEBSCAN__NETSPARKER_MANY_RESULTS__FAST("https://netsparker.manyfindings.demo." + ExampleConstants.URI_TARGET_SERVER),

	CODE_SCAN__CHECKMARX__YELLOW__FAST("../sechub-doc/src/main/java"),

	/**
     * runs 1 second - results in green
     */
	CODE_SCAN__CHECKMARX__GREEN__FAST("../../../../src"),

	/**
     * runs 10 milliseconds - results in green
     */
	CODE_SCAN__CHECKMARX__GREEN__SUPERFAST("../sechub-integrationtest/src/main/java"),
	
	/**
	 * runs 4 seconds - results in green
	 */
	CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING("../sechub-test/src/main/java"),

	NOT_PREDEFINED(null), 
	

	;

	private String target;
	private boolean isTargetUsableAsWhiteListEntry;

	private IntegrationTestMockMode(String target) {
		this.target = target;
		isTargetUsableAsWhiteListEntry = false;
		if (target != null) {
			try {
				new URI(target);
				isTargetUsableAsWhiteListEntry = true;
			} catch (URISyntaxException e) {
				/* means is no URI */
			}
		}
	}

	public String getTarget() {
		return target;
	}

	public boolean isTargetUsableAsWhitelistEntry() {
		return isTargetUsableAsWhiteListEntry;
	}

}
