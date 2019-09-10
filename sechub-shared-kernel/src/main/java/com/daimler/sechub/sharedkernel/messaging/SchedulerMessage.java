// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.util.JSONable;
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

	private boolean enabled;

	private int amountOfAllJobs;
	private int amountOfWaitingJobs;
	private int amountOfRunningJobs;

	public void setAmountOfJobsAll(int amountOfJobsAll) {
		this.amountOfAllJobs = amountOfJobsAll;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getAmountOfAllJobs() {
		return amountOfAllJobs;
	}

	public int getAmountOfWaitingJobs() {
		return amountOfWaitingJobs;
	}

	public int getAmountOfRunningJobs() {
		return amountOfRunningJobs;
	}

	public void setAmountOfRunningJobs(int amountOfRunningJobs) {
		this.amountOfRunningJobs = amountOfRunningJobs;
	}

	public void setAmountOfWaitingJobs(int amountOfWaitingJobs) {
		this.amountOfWaitingJobs = amountOfWaitingJobs;
	}

	@Override
	public Class<SchedulerMessage> getJSONTargetClass() {
		return SchedulerMessage.class;
	}

}
