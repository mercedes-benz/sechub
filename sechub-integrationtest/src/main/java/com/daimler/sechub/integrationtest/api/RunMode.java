// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public enum RunMode {

	/**
	 * No waits for executed mocks.
	 */
	WEBSCAN__RESULT_GREEN__FAST,

	/**
	 * Web and infra scans will have 10 seconds elapse time on mock execution.
	 */
	WEBSCAN__RESULT_GREEN__LONG_RUNNING,

	WEBSCAN__RESULT_ONE_FINDING__FAST,

	CODE_SCAN_YELLOW__FAST,

	CODE_SCAN_GREEN__FAST,


}
