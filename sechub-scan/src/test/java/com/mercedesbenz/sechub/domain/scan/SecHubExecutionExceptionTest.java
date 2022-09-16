// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.domain.scan.SecHubExecutionException.*;
import static org.hamcrest.Matchers.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class SecHubExecutionExceptionTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    @Test
    public void throwAsSecHubExecutionException_throws_adapter_exception_when_exception_is_runtime_exception() throws Exception {
        /* prepare */
        RuntimeException cause = new RuntimeException();

        /* test */
        expected.expect(SecHubExecutionException.class);
        expected.expectMessage("message1");
        expected.expectCause(equalTo(cause));

        /* execute */
        throwAsSecHubExecutionException("message1", cause);
    }

    @Test
    public void throwAsSecHubExecutionException_rethrows_adapter_exception_when_exception_is_adapter_exception() throws Exception {
        /* prepare */
        SecHubExecutionException cause = new SecHubExecutionException("originMessage");

        /* test */
        expected.expect(SecHubExecutionException.class);
        expected.expectMessage("originMessage");

        /* execute */
        throwAsSecHubExecutionException("otherMessage", cause);
    }

}
