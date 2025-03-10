// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConfiguration;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@Component
public class SecHubDirectCheckmarxResilienceConfiguration implements CheckmarxResilienceConfiguration {

    @Value("${sechub.adapter.checkmarx.resilience.badrequest.retry.max:" + DEFAULT_BADREQUEST_RETRY_MAX + "}")
    @MustBeDocumented(value = "Maximum amount of possible retries for situations when a 400 bad request happened on Checkmarx server", scope = SCOPE_CHECKMARX)
    private int badRequestMaxRetries = DEFAULT_BADREQUEST_RETRY_MAX;

    @Value("${sechub.adapter.checkmarx.resilience.badrequest.retry.wait:" + DEFAULT_BADREQUEST_RETRY_TIME_TO_WAIT_MILLISECONDS + "}")
    @MustBeDocumented(value = "Time to wait until retry is done when a 400 bad request happened on Checkmarx server", scope = SCOPE_CHECKMARX)
    private int badRequestRetryTimeToWaitInMilliseconds = DEFAULT_BADREQUEST_RETRY_TIME_TO_WAIT_MILLISECONDS;

    @Value("${sechub.adapter.checkmarx.resilience.servererror.retry.max:" + DEFAULT_SERVERERROR_RETRY_MAX + "}")
    @MustBeDocumented(value = "Maximum amount of possible retries for situations when a 500 server internal error happened on Checkmarx server", scope = SCOPE_CHECKMARX)
    private int internalServerErrortMaxRetries = DEFAULT_SERVERERROR_RETRY_MAX;

    @Value("${sechub.adapter.checkmarx.resilience.servererror.retry.wait:" + DEFAULT_SERVERERROR_RETRY_TIME_TO_WAIT_MILLISECONDS + "}")
    @MustBeDocumented(value = "Time to wait until retry is done when a 500 server internal error happened on Checkmarx server", scope = SCOPE_CHECKMARX)
    private int internalServerErrorRetryTimeToWaitInMilliseconds = DEFAULT_SERVERERROR_RETRY_TIME_TO_WAIT_MILLISECONDS;

    @Value("${sechub.adapter.checkmarx.resilience.networkerror.retry.max:" + DEFAULT_NETWORKERROR_RETRY_MAX + "}")
    @MustBeDocumented(value = "Maximum amount of possible retries for situations when a network error happened on communication to Checkmarx server", scope = SCOPE_CHECKMARX)
    private int networkErrortMaxRetries = DEFAULT_NETWORKERROR_RETRY_MAX;

    @Value("${sechub.adapter.checkmarx.resilience.networkerror.retry.wait:" + DEFAULT_SERVERERROR_RETRY_TIME_TO_WAIT_MILLISECONDS + "}")
    @MustBeDocumented(value = "Time to wait until retry is done when a network server happened on communication to Checkmarx server", scope = SCOPE_CHECKMARX)
    private int networkErrorTimeToWaitInMilliseconds = DEFAULT_NETWORKERROR_RETRY_TIME_TO_WAIT_MILLISECONDS;

    public int getBadRequestMaxRetries() {
        return badRequestMaxRetries;
    }

    public int getBadRequestRetryTimeToWaitInMilliseconds() {
        return badRequestRetryTimeToWaitInMilliseconds;
    }

    public int getInternalServerErrortMaxRetries() {
        return internalServerErrortMaxRetries;
    }

    public int getInternalServerErrorRetryTimeToWaitInMilliseconds() {
        return internalServerErrorRetryTimeToWaitInMilliseconds;
    }

    @Override
    public int getNetworkErrorMaxRetries() {
        return networkErrortMaxRetries;
    }

    @Override
    public int getNetworkErrorRetryTimeToWaitInMilliseconds() {
        return networkErrorTimeToWaitInMilliseconds;
    }

}
