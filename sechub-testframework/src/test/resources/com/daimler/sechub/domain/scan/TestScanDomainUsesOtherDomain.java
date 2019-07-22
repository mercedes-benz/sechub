// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import com.daimler.sechub.domain.administration.TestAdminDomain;

public class TestScanDomainUsesOtherDomain {
	/* Test case scenario - will be recognized by PackageStructureTest */ 
	public static void main(String[] args) {
		TestAdminDomain illegalAccessToAnotherDomain = new TestAdminDomain();
		illegalAccessToAnotherDomain.showAccess();
	}
}
