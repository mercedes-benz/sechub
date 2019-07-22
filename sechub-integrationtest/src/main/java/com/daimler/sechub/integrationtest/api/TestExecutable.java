// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public interface TestExecutable {
	/**
	 * @return false when stop execution, true when still need to execute
	 */
	public boolean run() throws Exception;

	public boolean wasSuccessful();

	public int getTimeoutInSeconds();

	public long getTimeToWaitInMillis();

	public Class<? extends Exception>[] getHandledExceptions();

	TestUser getUser();

}