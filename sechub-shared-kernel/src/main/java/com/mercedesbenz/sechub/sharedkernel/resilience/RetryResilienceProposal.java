// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.resilience;

public interface RetryResilienceProposal extends ResilienceProposal {

    public int getMaximumAmountOfRetries();

    public long getMillisecondsToWaitBeforeRetry();

}
