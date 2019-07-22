// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public enum RunMode {

	/**
	 * No waits for executed mocks.
	 */
	NORMAL,

	/**
	 * Web and infra scans will have 10 seconds elapse time on mock execution.
	 */
	LONG_RUNNING_BUT_GREEN,


}
