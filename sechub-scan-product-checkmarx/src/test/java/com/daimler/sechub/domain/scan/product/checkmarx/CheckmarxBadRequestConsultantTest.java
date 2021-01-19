// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;
import com.daimler.sechub.sharedkernel.resilience.ResilienceProposal;
import com.daimler.sechub.sharedkernel.resilience.RetryResilienceProposal;

public class CheckmarxBadRequestConsultantTest {

    private CheckmarxResilienceConsultant consultantToTest;
    private ResilienceContext context;

    @Before
    public void before() {
        consultantToTest = new CheckmarxResilienceConsultant();
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
    public void http_bad_request_400_exception_returns_retry_proposal_with_3_retries_and_2000_millis_to_wait() {
        /* prepare */
        when(context.getCurrentError()).thenReturn(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(3, rrp.getMaximumAmountOfRetries());
        assertEquals(2000, rrp.getMillisecondsToWaitBeforeRetry());
    }

    @Test
    public void http_bad_server_error_500_exception_returns_retry_proposal_with_1_retries_and_5000_millis_to_wait() {
        /* prepare */
        when(context.getCurrentError()).thenReturn(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(1, rrp.getMaximumAmountOfRetries());
        assertEquals(5000, rrp.getMillisecondsToWaitBeforeRetry());
    }

    @Test
    public void nested_http_bad_request_400_exception_wrapped_in_runtime_and_sechubexecution_exception_returns_retry_proposal_with_3_retries_and_2000_millis_to_wait() {

        /* prepare */
        when(context.getCurrentError())
                .thenReturn(new SecHubExecutionException("se1", new RuntimeException(new HttpClientErrorException(HttpStatus.BAD_REQUEST))));

        /* execute */
        ResilienceProposal proposal = consultantToTest.consultFor(context);

        /* test */
        assertNotNull(proposal);
        assertTrue(proposal instanceof RetryResilienceProposal);
        RetryResilienceProposal rrp = (RetryResilienceProposal) proposal;
        assertEquals(3, rrp.getMaximumAmountOfRetries());
        assertEquals(2000, rrp.getMillisecondsToWaitBeforeRetry());
    }

}
