// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class StacktraceUtilTest {

    @Test
    public void runtime_exception_containing_a_HttpClientErrorException_find_root_HttpClientErrorException() {
        /* prepare */
        Throwable wantedrootCause = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        RuntimeException e = new RuntimeException("test", wantedrootCause);

        /* execute */
        Throwable rootCause = StacktraceUtil.findRootCause(e);

        /* test */
        assertEquals(wantedrootCause, rootCause);
    }

    @Test
    public void runtime_exception_containing_no_cause_returns_runtime_exception() {
        /* prepare */
        RuntimeException e = new RuntimeException("test");

        /* execute */
        Throwable rootCause = StacktraceUtil.findRootCause(e);

        /* test */
        assertEquals(e, rootCause);
    }

    @Test
    public void null_given_returns_null() {
        /* prepare */

        /* execute */
        Throwable rootCause = StacktraceUtil.findRootCause(null);

        /* test */
        assertEquals(null, rootCause);
    }

}
