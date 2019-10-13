// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public enum RunMode {

	/**
	 * No waits for executed mocks.
	 */
	WEBSCAN__NETSPARKER_RESULT_GREEN__FAST,

	/**
	 * Web and infra scans will have 10 seconds elapse time on mock execution.
	 */
	WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING,

	WEBSCAN__NETSPARKER_RESULT_ONE_FINDING__FAST,

	WEBSCAN__NETSPARKER_MANY_RESULTS__FAST,

	CODE_SCAN__CHECKMARX__YELLOW__FAST,

	CODE_SCAN__CHECKMARX__GREEN__FAST,


}
