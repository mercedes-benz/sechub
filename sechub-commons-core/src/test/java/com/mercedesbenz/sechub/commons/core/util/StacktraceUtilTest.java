// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StacktraceUtilTest {

    @Test
    void runtime_exception_having_IllegalStateException_as_root_cause() {
        /* prepare */
        Throwable wantedrootCause = new IllegalStateException("teststate");
        RuntimeException e = new RuntimeException("test", wantedrootCause);

        /* execute */
        Throwable rootCause = StacktraceUtil.findRootCause(e);

        /* test */
        assertEquals(wantedrootCause, rootCause);
    }

    @Test
    void runtime_exception_containing_no_cause_returns_runtime_exception() {
        /* prepare */
        RuntimeException e = new RuntimeException("test");

        /* execute */
        Throwable rootCause = StacktraceUtil.findRootCause(e);

        /* test */
        assertEquals(e, rootCause);
    }

    @Test
    void null_given_returns_null() {
        /* prepare */

        /* execute */
        Throwable rootCause = StacktraceUtil.findRootCause(null);

        /* test */
        assertEquals(null, rootCause);
    }

}
