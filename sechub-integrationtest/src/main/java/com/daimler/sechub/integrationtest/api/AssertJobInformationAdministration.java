// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

public class AssertJobInformationAdministration extends AbstractAssert {

	private static final int DEFAULT_TIMEOUT_MS = 5000;
	private TestUser user;

	/**
	 * Creates assert object - if user is able to fetch job list...
	 *
	 * @param user
	 */
	public AssertJobInformationAdministration(TestUser user) {
		this.user = user;
	}

	public AssertJobInformation canFindRunningJob(UUID jobUUID) {
		return canFindRunningJob(jobUUID,DEFAULT_TIMEOUT_MS);
	}

	public AssertJobInformation canFindRunningJob(UUID jobUUID, long timeOutInMilliseconds) {
		return new AssertJobInformation(jobUUID, true,timeOutInMilliseconds);
	}
	public AssertJobInformationAdministration canNotFindRunningJob(UUID jobUUID) {
		return canNotFindRunningJob(jobUUID, DEFAULT_TIMEOUT_MS);
	}
	public AssertJobInformationAdministration canNotFindRunningJob(UUID jobUUID, long timeOutInMilliseconds) {
		new AssertJobInformation(jobUUID, false,timeOutInMilliseconds);
		return this;
	}

	public class AssertJobInformation {

		public AssertJobInformation(UUID jobUUID, boolean expected, long timeOutInMilliseconds) {
			internalCheck(jobUUID, expected, timeOutInMilliseconds);
		}

		private void internalCheck(UUID jobUUID, boolean expected,long timeOutInMilliseconds) {
			long start = System.currentTimeMillis();
			boolean timeElapsed=false;
			while (!timeElapsed) { /*NOSONAR*/

				timeElapsed= System.currentTimeMillis()-start>timeOutInMilliseconds;

				String json = getRestHelper(user).getJSon(getUrlBuilder().buildAdminFetchAllRunningJobsUrl());
				/* very simple ... maybe this should be improved... */
				boolean found = json != null && json.contains("\"" + jobUUID);
				if (expected) {
					if (found) {
						/* oh found - done */
						break;
					}else if ( timeElapsed) {
						fail("JSON did not contain:\n" + jobUUID + "\nwas:\n" + json+"\n (waited :"+timeElapsed+" milliseconds!)");
					}
				} else {
					if (!found) {
						/* oh not found - done */
						break;
					}else if (timeElapsed) {
						fail("JSON DID contain:\n" + jobUUID + "\nwas:\n" + json +"\n (waited :"+timeElapsed+" milliseconds!)");
					}
				}
				TestAPI.waitMilliSeconds(300);
			}
		}
	}

}
