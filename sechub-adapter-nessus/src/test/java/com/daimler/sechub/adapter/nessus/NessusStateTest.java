// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import static org.junit.Assert.*;

import org.junit.Test;

public class NessusStateTest {

	@Test
	public void is_wellknown_return_true_for_Completed() {
		assertTrue(NessusState.isWellknown("completed"));
	}

	@Test
	public void is_wellknown_return_false_for_Unknown() {
		assertFalse(NessusState.isWellknown("unknown"));
	}
}
