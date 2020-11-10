// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OneInstallSetupTest {
	
	private static final String HOSTNAME_INTRANET = "hostnameIntranet";
	private static final String HOSTNAME_INTERNET = "hostnameInternet";
	
	private String identifierInternetTarget;
	private String identifierIntranetTarget;

	@Before
	public void before() throws Exception {
		
		identifierIntranetTarget=null;
		
		identifierInternetTarget=null;
	}
	
	@Test
	public void intranet_set__internet_set() {
		/* prepare */
		identifierIntranetTarget=HOSTNAME_INTRANET;
		
		identifierInternetTarget=HOSTNAME_INTERNET;
		
		/* test */
		expectCanScan(TargetType.INTERNET);
		expectCanScan(TargetType.INTRANET);
	}
	
	@Test
	public void intranet_not_set__internet_not_set() {
		/* prepare - not necessary, everything null*/
		
		/* test */
		expectCannotScan(TargetType.INTERNET);
		expectCannotScan(TargetType.INTRANET);
	}
	
	@Test
	public void intranet_set__internet_not_set() {
		/* prepare */
		identifierIntranetTarget=HOSTNAME_INTRANET;
		
		/* test */
		expectCanScan(TargetType.INTRANET);
		expectCannotScan(TargetType.INTERNET);
	}
	

	@Test
	public void intranet_not_set__internet_set() {
		/* prepare */
		identifierInternetTarget=HOSTNAME_INTERNET;
		
		/* test */
		expectCannotScan(TargetType.INTRANET);
		expectCanScan(TargetType.INTERNET);
	}
	
	private void expectCanScan(TargetType type) {
		expectCanOrCannotScan(true, type);
	}
	private void expectCannotScan(TargetType type) {
		expectCanOrCannotScan(false, type);
		
	}
	private void expectCanOrCannotScan(boolean expectedCan, TargetType type) {
		/* execute */
		TestOneInstallSetup toTest = new TestOneInstallSetup();
		
		/* test */
		boolean ableToScan = toTest.isAbleToScan(type);
		if (expectedCan) {
			assertTrue("Expected to be able to scan "+type.name()+" but is NOT!", ableToScan);
		}else {
			assertFalse("Expected NOT to be able to scan "+type.name()+" but is!", ableToScan);
		}
	}

	private class TestOneInstallSetup extends AbstractTargetIdentifyingOneInstallSetup {

		@Override
		protected String getIdentifierWhenInternetTarget() {
			return identifierInternetTarget;
		}

		@Override
		protected String getIdentifierWhenIntranetTarget() {
			return identifierIntranetTarget;
		}

		@Override
		public String getUserId() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public String getBaseURL() {
			return null;
		}

		@Override
		public boolean isHavingUntrustedCertificate() {
			return false;
		}


	}
}
