// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.SocketException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.mercedesbenz.sechub.commons.core.resilience.ResilienceContext;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceProposal;
import com.mercedesbenz.sechub.commons.core.resilience.RetryResilienceProposal;

public class CheckmarxResilienceConsultantTest {

    private static final int TESTCONFIG_BAD_REQUEST_MAX_RETRIES = 10;
    private static final int TESTCONFIG_BAD_REQUEST_RETRY_TIME_TO_WAIT_MILLIS = 20;

    private static final int TESTCONFIG_SERVERERROR_MAX_RETRIES = 30;
    private static final int TESTCONFIG_SERVERERROR_RETRY_TIME_TO_WAIT_MILLIS = 40;

    private static final int TESTCONFIG_NETWORKERROR_MAX_RETRIES = 50;
    private static final int TESTCONFIG_NETWORKERROR_RETRY_TIME_TO_WAIT_MILLIS = 60;

    private CheckmarxResilienceConsultant consultantToTest;
    private ResilienceContext context;

    @Before
    public void before() {
        CheckmarxResilienceConfiguration config = mock(CheckmarxResilienceConfiguration.class);
        when(config.getBadRequestMaxRetries()).thenReturn(TESTCONFIG_BAD_REQUEST_MAX_RETRIES);
        when(config.getBadRequestRetryTimeToWaitInMilliseconds()).thenReturn(TESTCONFIG_BAD_REQUEST_RETRY_TIME_TO_WAIT_MILLIS);

        when(config.getInternalServerErrortMaxRetries()).thenReturn(TESTCONFIG_SERVERERROR_MAX_RETRIES);
        when(config.getInternalServerErrorRetryTimeToWaitInMilliseconds()).thenReturn(TESTCONFIG_SERVERERROR_RETRY_TIME_TO_WAIT_MILLIS);

        when(config.getNetworkErrorMaxRetries()).thenReturn(TESTCONFIG_NETWORKERROR_MAX_RETRIES);
        when(config.getNetworkErrorRetryTimeToWaitInMilliseconds()).thenReturn(TESTCONFIG_NETWORKERROR_RETRY_TIME_TO_WAIT_MILLIS);

        consultantToTest = new CheckmarxResilienceConsultant(config);
        context = mock(ResilienceContext.class);
    }

    @Test
    public void no_exception_returns_null() {
        /* prepare */

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNull(proposal);
    }

    @Test
    public void illegal_argument_exception_returns_null() {
        /* prepare */
        when(context.getCurrentError()).thenReturn(new IllegalArgumentException());

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNull(proposal);
    }

    @Test
    public void http_bad_request_400_exception_returns_retry_proposal_with_with_badrequest_config() {
        /* prepare */
        when(context.getCurrentError()).thenReturn(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(TESTCONFIG_BAD_REQUEST_MAX_RETRIES, rrp.getMaximumAmountOfRetries());
        assertEquals(TESTCONFIG_BAD_REQUEST_RETRY_TIME_TO_WAIT_MILLIS, rrp.getMillisecondsToWaitBeforeRetry());
    }

    @Test
    public void http_bad_server_error_500_exception_returns_retry_proposal_with_servererror_config() {
        /* prepare */
        when(context.getCurrentError()).thenReturn(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(TESTCONFIG_SERVERERROR_MAX_RETRIES, rrp.getMaximumAmountOfRetries());
        assertEquals(TESTCONFIG_SERVERERROR_RETRY_TIME_TO_WAIT_MILLIS, rrp.getMillisecondsToWaitBeforeRetry());
    }

    @Test
    public void socket_exception_returns_retry_proposal_with_networkerror_config() {
        /* prepare */
        when(context.getCurrentError()).thenReturn(new SocketException());

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(TESTCONFIG_NETWORKERROR_MAX_RETRIES, rrp.getMaximumAmountOfRetries());
        assertEquals(TESTCONFIG_NETWORKERROR_RETRY_TIME_TO_WAIT_MILLIS, rrp.getMillisecondsToWaitBeforeRetry());
    }

    @Test
    public void nested_http_bad_request_400_exception_wrapped_in_runtime_and_sechubexecution_exception_returns_retry_proposal_with_badrequest_config() {

        /* prepare */
        when(context.getCurrentError())
                .thenReturn(new IOException("se1", new RuntimeException(new HttpClientErrorException(HttpStatus.BAD_REQUEST))));

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(TESTCONFIG_BAD_REQUEST_MAX_RETRIES, rrp.getMaximumAmountOfRetries());
        assertEquals(TESTCONFIG_BAD_REQUEST_RETRY_TIME_TO_WAIT_MILLIS, rrp.getMillisecondsToWaitBeforeRetry());
    }

}
