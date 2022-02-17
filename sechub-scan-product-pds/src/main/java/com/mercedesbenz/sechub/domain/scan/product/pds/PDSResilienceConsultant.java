// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.resilience.ResilienceConsultant;
import com.mercedesbenz.sechub.sharedkernel.resilience.ResilienceContext;
import com.mercedesbenz.sechub.sharedkernel.resilience.ResilienceProposal;
import com.mercedesbenz.sechub.sharedkernel.resilience.SimpleRetryResilienceProposal;
import com.mercedesbenz.sechub.sharedkernel.util.StacktraceUtil;

@Component
public class PDSResilienceConsultant implements ResilienceConsultant {

    private static final Logger LOG = LoggerFactory.getLogger(PDSResilienceConsultant.class);
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
            LOG.info("Can't make proposal for exception with root cause:{}", StacktraceUtil.createDescription(rootCause));
        }
        return null;
    }

}
