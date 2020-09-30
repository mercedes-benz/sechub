// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This message data object contains all possible information about scheduler
 * status
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) schedule domain and administration - and maybe others")
public class SchedulerMessage implements JSONable<SchedulerMessage> {

	private boolean jobProcessingEnabled;

	private long amountOfAllJobs;
	private long amountOfWaitingJobs;
	private long amountOfRunningJobs;

	public void setAmountOfJobsAll(long amountOfJobsAll) {
		this.amountOfAllJobs = amountOfJobsAll;
	}

	public boolean isJobProcessingEnabled() {
		return jobProcessingEnabled;
	}

	public void setJobProcessingEnabled(boolean enabled) {
		this.jobProcessingEnabled = enabled;
	}

	public long getAmountOfAllJobs() {
		return amountOfAllJobs;
	}

	public long getAmountOfWaitingJobs() {
		return amountOfWaitingJobs;
	}

	public long getAmountOfRunningJobs() {
		return amountOfRunningJobs;
	}

	public void setAmountOfRunningJobs(long amountOfRunningJobs) {
		this.amountOfRunningJobs = amountOfRunningJobs;
	}

	public void setAmountOfWaitingJobs(long amountOfWaitingJobs) {
		this.amountOfWaitingJobs = amountOfWaitingJobs;
	}

	@Override
	public Class<SchedulerMessage> getJSONTargetClass() {
		return SchedulerMessage.class;
	}

}
