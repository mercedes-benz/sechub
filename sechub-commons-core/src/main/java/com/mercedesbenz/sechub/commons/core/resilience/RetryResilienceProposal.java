// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public interface RetryResilienceProposal extends ResilienceProposal {

    public int getMaximumAmountOfRetries();

    public long getMillisecondsToWaitBeforeRetry();

}
