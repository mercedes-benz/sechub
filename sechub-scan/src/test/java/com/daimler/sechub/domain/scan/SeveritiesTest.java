// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.commons.model.Severities;
import com.daimler.sechub.commons.model.Severity;

public class SeveritiesTest {

	@Test
	public void check_all_severity_enum_parts_are_handled_one_time() {
		/* sanity check */
		assertEquals(Severity.values().length, Severities.getAllOrderedFromHighToLow().length);
		
		for (Severity severity : Severity.values()) {
			int found=0;
			for (Severity inside: Severities.getAllOrderedFromHighToLow()) {
				if (severity.equals(inside)) {
					found++;
				}
			}
			if (found!=1) {
				fail("Severity :"+severity+" should be found one time, but was found:"+found+" times!");
			}
		}
	}

}
