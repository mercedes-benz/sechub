// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

/**
 * A simple class to handle retries. A maximum is defined at constructor time.
 * By a an getter it is possible to determine if next retry is possible or not.
 * <br>
 * <br>
 *
 * @author Albert Tregnaghi
 *
 */
public class RetryContext {

	private int maximum;
	private int executionFailedCount;
	private long timeToWaitOnRetryDoneInMillis;
	private boolean executionDone;
	private boolean fatalFailure;

	/**
	 * Defining a maximum of 1 means: one execution failed no retry possible. maximim=2 means. one execution failed retry possible, another failed no retry possible
	 * @param maximum
	 */
	public RetryContext(int maximum) {
		this.maximum = maximum;
	}

	/**
	 * Set time to wait after one retry is done
	 * @param timeToWaitInMillis
	 */
	public RetryContext setRetryTimeToWait(long timeToWaitInMillis) {
		this.timeToWaitOnRetryDoneInMillis = timeToWaitInMillis;
		return this;
	}

	public void markAsFatalFailure() {
		this.executionDone=false;
		this.executionFailedCount++;
		this.fatalFailure=true;
	}

	/**
	 * @return <code>true</code> when more retries are possible and not already done
	 */
	public boolean isRetryPossible() {
		if (fatalFailure) {
			return false;
		}
		if (executionDone) {
			return false;
		}
		return maximum > executionFailedCount;
	}

	public int getExecutionFailedCount() {
		return executionFailedCount;
	}

	/**
	 * Mark this execution as failed and waits time defined by {@link #setRetryTimeToWait(long)}
	 */
	public void executionFailed() {
		executionFailedCount++;
		waitForNextRetry();
	}

	/**
	 * Mark execution as done. So no retries possible any longer!
	 */
	public void executionDone() {
		this.executionDone=true;
	}

	public boolean isExecutionDone() {
		return executionDone;
	}

	private void waitForNextRetry() {
		if (timeToWaitOnRetryDoneInMillis==0) {
			return;
		}
		try {
			Thread.sleep(timeToWaitOnRetryDoneInMillis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}


}
