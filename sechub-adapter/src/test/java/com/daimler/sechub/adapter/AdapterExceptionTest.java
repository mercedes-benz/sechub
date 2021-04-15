// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static com.daimler.sechub.adapter.AdapterException.*;
import static org.hamcrest.Matchers.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class AdapterExceptionTest {

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();

	@Test
	public void throwAsAdapterException_throws_adapter_exception_when_exception_is_runtime_exception()
			throws Exception {
		/* prepare */
		RuntimeException cause = new RuntimeException();

		/* test */
		expected.expect(AdapterException.class);
		expected.expectMessage("x:message1");
		expected.expectCause(equalTo(cause));

		/* execute */
		throwAsAdapterException(new AdapterLogId("x",null),"message1", cause);
	}
	
	@Test
	public void throwAsAdapterException_throws_adapter_exception_with_traceid_when_exception_is_runtime_exception()
			throws Exception {
		/* prepare */
		RuntimeException cause = new RuntimeException();

		/* test */
		expected.expect(AdapterException.class);
		expected.expectMessage("traceId x:message1");
		expected.expectCause(equalTo(cause));

		/* execute */
		throwAsAdapterException(new AdapterLogId("x","traceId"),"message1", cause);
	}

	@Test
	public void throwAsAdapterException_rethrows_adapter_exception_when_exception_is_adapter_exception()
			throws Exception {
		/* prepare */
		AdapterException cause = new AdapterException(new AdapterLogId("y",null),"originMessage");

		/* test */
		expected.expect(AdapterException.class);
		expected.expectMessage("y:originMessage");

		/* execute */
		throwAsAdapterException(new AdapterLogId("z",null),"otherMessage", cause);
	}

}
