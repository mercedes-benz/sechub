// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.net.SocketException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import com.mercedesbenz.sechub.commons.core.resilience.ResilienceConsultant;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceContext;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceProposal;
import com.mercedesbenz.sechub.commons.core.resilience.SimpleRetryResilienceProposal;
import com.mercedesbenz.sechub.commons.core.util.StacktraceUtil;

public class PDSAdapterResilienceConsultant implements ResilienceConsultant {

    private static final Logger LOG = LoggerFactory.getLogger(PDSAdapterResilienceConsultant.class);

    public static final long DEFAULT_RETRY_TIME_TO_WAIT_IN_MILLISECONDS = 10000;
    public static final int DEFAULT_MAX_RETRIES = 3;

    private int maxRetries;

    private long retryTimeToWaitInMilliseconds;

    public PDSAdapterResilienceConsultant() {
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.retryTimeToWaitInMilliseconds = DEFAULT_RETRY_TIME_TO_WAIT_IN_MILLISECONDS;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setRetryTimeToWaitInMilliseconds(long retryTimeToWaitInMilliseconds) {
        this.retryTimeToWaitInMilliseconds = retryTimeToWaitInMilliseconds;
    }

    public long getRetryTimeToWaitInMilliseconds() {
        return retryTimeToWaitInMilliseconds;
    }

    @Override
    public ResilienceProposal consultFor(ResilienceContext context) {
        Objects.requireNonNull(context);

        Exception currentError = context.getCurrentError();
        if (currentError instanceof ResourceAccessException) {
            return createProposalForNetworkProblem("Handle resource access exception");
        }
        Throwable rootCause = StacktraceUtil.findRootCause(currentError);
        if (rootCause instanceof SocketException) {
            LOG.info("Propose retry for socket exception");
            return createProposalForNetworkProblem("Socket exception handling");
        }

        LOG.info("Can't make proposal for exception with root cause:{}", StacktraceUtil.createDescription(rootCause));
        return null;
    }

    private ResilienceProposal createProposalForNetworkProblem(String message) {
        return new SimpleRetryResilienceProposal(message, maxRetries, retryTimeToWaitInMilliseconds);
    }

}
