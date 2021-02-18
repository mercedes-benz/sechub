// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.resilience.ResilienceConsultant;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;
import com.daimler.sechub.sharedkernel.resilience.ResilienceProposal;
import com.daimler.sechub.sharedkernel.resilience.SimpleRetryResilienceProposal;
import com.daimler.sechub.sharedkernel.util.StacktraceUtil;

@Component
public class CheckmarxResilienceConsultant implements ResilienceConsultant {

    public static final String CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN = "checkmarx.resilience.fallback.fullscan";

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxResilienceConsultant.class);
    private static final int DEFAULT_BADREQUEST_RETRY_MAX = 3;
    private static final int DEFAULT_BADREQUEST_RETRY_TIMEOUT_MILLISECONDS = 2000;
    private static final int DEFAULT_SERVERERROR_RETRY_MAX = 1;
    private static final int DEFAULT_SERVERERROR_RETRY_TIMEOUT_MILLISECONDS = 5000;

    @Value("${sechub.adapter.checkmarx.resilience.badrequest.retry.max:" + DEFAULT_BADREQUEST_RETRY_MAX + "}")
    @MustBeDocumented("Amount of retries done when a 400 bad request happened on Checkmarx server")
    private int badRequestMaxRetries = DEFAULT_BADREQUEST_RETRY_MAX;

    @Value("${sechub.adapter.checkmarx.resilience.badrequest.retry.wait:" + DEFAULT_BADREQUEST_RETRY_TIMEOUT_MILLISECONDS + "}")
    @MustBeDocumented("Time to wait until retry is done when a 400 bad request happened on Checkmarx server")
    private int badRequestRetryTimeToWaitInMilliseconds = DEFAULT_BADREQUEST_RETRY_TIMEOUT_MILLISECONDS;

    @Value("${sechub.adapter.checkmarx.resilience.servererror.retry.max:" + DEFAULT_SERVERERROR_RETRY_MAX + "}")
    @MustBeDocumented("Amount of retries done when a 500 server internal error happened on Checkmarx server")
    private int internalServerErrortMaxRetries = DEFAULT_SERVERERROR_RETRY_MAX;

    @Value("${sechub.adapter.checkmarx.resilience.servererror.retry.wait:" + DEFAULT_SERVERERROR_RETRY_TIMEOUT_MILLISECONDS + "}")
    @MustBeDocumented("Time to wait until retry is done when a 500 server internal error happened on Checkmarx server")
    private int internalServerErrorTimeToWaitInMilliseconds = DEFAULT_SERVERERROR_RETRY_TIMEOUT_MILLISECONDS;

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

        if (rootCause instanceof HttpClientErrorException) {
            HttpClientErrorException hce = (HttpClientErrorException) rootCause;
            int statusCode = hce.getRawStatusCode();
            if (statusCode == 400) {
                /*
                 * BAD request - this can happen for same project scans put to queue because
                 * there can a CHECKMARX server error happen
                 */
                LOG.info("Propose retry for bad request");
                return new SimpleRetryResilienceProposal("checkmarx bad request handling", badRequestMaxRetries, badRequestRetryTimeToWaitInMilliseconds);

            } else if (statusCode == 500) {
                /*
                 * An internal server error happened - lets assume that this is temporary and do
                 * a retry
                 */
                LOG.info("Propose retry for internal server error");
                return new SimpleRetryResilienceProposal("checkmarx internal server error handling", internalServerErrortMaxRetries,
                        internalServerErrorTimeToWaitInMilliseconds);

            } else {
                LOG.info("Can't make proposal for http client error exception:{}", StacktraceUtil.createDescription(rootCause));
            }
        } else {
            // unexpected problem - so log as warning and exception, so full stack trace availabe in logs
            LOG.warn("Can't make proposal for exception with root cause: {}",rootCause.getClass().getSimpleName(),rootCause);
        }
        return null;
    }

}
