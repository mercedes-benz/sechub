// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.domain.administration.TestAdminDomain;

public class TestScanDomainUsesOtherDomain {
	/* Test case scenario - will be recognized by PackageStructureTest */ 
	public static void main(String[] args) {
		TestAdminDomain illegalAccessToAnotherDomain = new TestAdminDomain();
		illegalAccessToAnotherDomain.showAccess();
	}
}
