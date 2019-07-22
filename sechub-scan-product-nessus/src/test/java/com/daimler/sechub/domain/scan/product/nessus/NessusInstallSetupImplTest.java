// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.nessus;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.TargetIdentifyingMultiInstallSetup;

public class NessusInstallSetupImplTest {
	private NessusInstallSetupImpl setupToTest;

	@Before
	public void before() {
		setupToTest = new NessusInstallSetupImpl();
	}
	
	@Test
	public void is_extending_double_install() {
		assertTrue(setupToTest instanceof TargetIdentifyingMultiInstallSetup);
	}

}
