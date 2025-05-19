// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import java.net.SocketException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.mercedesbenz.sechub.commons.core.resilience.ResilienceConsultant;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceContext;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceProposal;
import com.mercedesbenz.sechub.commons.core.resilience.SimpleRetryResilienceProposal;
import com.mercedesbenz.sechub.commons.core.util.StacktraceUtil;

public class CheckmarxResilienceConsultant implements ResilienceConsultant {

    public static final String CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN = "checkmarx.resilience.fallback.fullscan";

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxResilienceConsultant.class);

    CheckmarxResilienceConfiguration resilienceConfig;

    public CheckmarxResilienceConsultant(CheckmarxResilienceConfiguration resilienceConfig) {
        this.resilienceConfig = resilienceConfig;
    }

    public CheckmarxResilienceConfiguration getResilienceConfig() {
        return resilienceConfig;
    }

    @Override
    public ResilienceProposal consultFor(ResilienceContext context) {
        Objects.requireNonNull(context, "Resilience context must not be null");

        Throwable rootCause = StacktraceUtil.findRootCause(context.getCurrentError());
        if (rootCause == null) {
            LOG.warn("Cannot make any proposal when root cause is null!");
            return null;
        }

        if (isThresholdLimitExceeded(rootCause)) {
            return handleThresholdLimitExceeded(context);
        }

        if (rootCause instanceof SocketException) {
            return handleSocketException();
        }

        if (rootCause instanceof RestClientException) {
            return handleRestClientException(rootCause);
        }

        LOG.warn("No proposal available for exception: {}", rootCause.getClass().getSimpleName());
        return null;
    }

    private boolean isThresholdLimitExceeded(Throwable rootCause) {
        String message = rootCause.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("Changes exceeded the threshold limit");
    }

    private ResilienceProposal handleThresholdLimitExceeded(ResilienceContext context) {
        LOG.warn("Checkmarx delta scan exceeded threshold limit. Will suggest to do a retry with fullscan enabled");
        context.setValue(CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN, true);

        return new SimpleRetryResilienceProposal("checkmarx too many changes - retry fullscan handling", 1, 500);
    }

    private ResilienceProposal handleSocketException() {
        LOG.info("Propose retry for socket exception");
        return new SimpleRetryResilienceProposal("checkmarx network error handling", resilienceConfig.getNetworkErrorMaxRetries(),
                resilienceConfig.getNetworkErrorRetryTimeToWaitInMilliseconds());
    }

    private ResilienceProposal handleRestClientException(Throwable rootCause) {
        if (rootCause instanceof HttpStatusCodeException) {
            return handleHttpStatusCodeException((HttpStatusCodeException) rootCause);
        }

        LOG.warn("Unexpected RestClientException: {}", StacktraceUtil.createDescription(rootCause));
        return null;
    }

    private ResilienceProposal handleHttpStatusCodeException(HttpStatusCodeException exception) {
        int statusCode = exception.getStatusCode().value();

        if (statusCode == 400) {
            LOG.info("Propose retry for bad request");
            return new SimpleRetryResilienceProposal("checkmarx bad request handling", resilienceConfig.getBadRequestMaxRetries(),
                    resilienceConfig.getBadRequestRetryTimeToWaitInMilliseconds());
        }

        if (statusCode >= 500 && statusCode <= 599) {
            LOG.info("Propose retry for server error with status code: {}", statusCode);
            return new SimpleRetryResilienceProposal("checkmarx server error handling", resilienceConfig.getInternalServerErrortMaxRetries(),
                    resilienceConfig.getInternalServerErrorRetryTimeToWaitInMilliseconds());
        }

        LOG.warn("No proposal for HTTP status code: {}", statusCode);
        return null;
    }
}
