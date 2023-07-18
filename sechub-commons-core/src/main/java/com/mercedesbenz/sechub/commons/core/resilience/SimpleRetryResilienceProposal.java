// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public class SimpleRetryResilienceProposal implements RetryResilienceProposal {

    private int maximumAmountOfRetries;
    private long milliSecondsToWaitBeforeRetry;
    private String info;

    /**
     * Creates a retry proposal
     *
     * @param info                          contains information about reason why
     *                                      retry was necessary etc.
     * @param maximumAmountOfRetries
     * @param milliSecondsToWaitBeforeRetry
     */
    public SimpleRetryResilienceProposal(String info, int maximumAmountOfRetries, long milliSecondsToWaitBeforeRetry) {
        this.info = info;
        this.maximumAmountOfRetries = maximumAmountOfRetries;
        this.milliSecondsToWaitBeforeRetry = milliSecondsToWaitBeforeRetry;
    }

    @Override
    public int getMaximumAmountOfRetries() {
        return maximumAmountOfRetries;
    }

    @Override
    public long getMillisecondsToWaitBeforeRetry() {
        return milliSecondsToWaitBeforeRetry;
    }

    @Override
    public String getInfo() {
        return info;
    }

}
