// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import java.net.SocketException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

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
        Objects.requireNonNull(context);
        Throwable rootCause = StacktraceUtil.findRootCause(context.getCurrentError());
        if (rootCause == null) {
            LOG.warn("Cannot make any proposal when root cause is null!");
            return null;
        }
        String message = rootCause.getMessage();

        if (message != null) {
            if (message.contains("Changes exceeded the threshold limit")) {
                LOG.warn("Checkmarx delta scan exceeded treshold limit. Will suggest to do a retry with fullscan enabled");
                context.setValue(CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN, true);

                return new SimpleRetryResilienceProposal("checkmarx too many changes - retry fullscan handling", 1, 500);
            }
        }

        if (rootCause instanceof SocketException) {
            LOG.info("Propose retry for socket exception");
            return new SimpleRetryResilienceProposal("checkmarx network error handling", resilienceConfig.getNetworkErrorMaxRetries(),
                    resilienceConfig.getNetworkErrorRetryTimeToWaitInMilliseconds());
        }

        if (rootCause instanceof HttpClientErrorException) {
            HttpClientErrorException hce = (HttpClientErrorException) rootCause;
            int statusCode = hce.getRawStatusCode();
            if (statusCode == 400) {
                /*
                 * BAD request - this can happen for same project scans put to queue because
                 * there can a CHECKMARX server error happen
                 */
                LOG.info("Propose retry for bad request");
                return new SimpleRetryResilienceProposal("checkmarx bad request handling", resilienceConfig.getBadRequestMaxRetries(),
                        resilienceConfig.getBadRequestRetryTimeToWaitInMilliseconds());

            } else if (statusCode == 500) {
                /*
                 * An internal server error happened - lets assume that this is temporary and do
                 * a retry
                 */
                LOG.info("Propose retry for internal server error");
                return new SimpleRetryResilienceProposal("checkmarx internal server error handling", resilienceConfig.getInternalServerErrortMaxRetries(),
                        resilienceConfig.getInternalServerErrorRetryTimeToWaitInMilliseconds());

            } else {
                LOG.info("Can't make proposal for http client error exception:{}", StacktraceUtil.createDescription(rootCause));
            }
        } else {
            // unexpected problem - so log as warning and exception, so full stack trace
            // availabe in logs
            LOG.warn("Can't make proposal for exception with root cause: {}", rootCause.getClass().getSimpleName(), rootCause);
        }
        return null;
    }

}
