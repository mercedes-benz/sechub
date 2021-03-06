// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AbstractMultiInstallSetupTest {
	
	private static final String PASSWORD_INTERNET = "pwdInternet";
	private static final String PASSWORD_INTRANET = "pwdIntranet";
	private static final String USERNAME_INTRANET = "useridIntranet";
	private static final String USERNAME_INTERNET = "useridInternet";
	private static final String BASEURL_INTRANET = "baseURLIntranet";
	private static final String BASEURL_INTERNET = "baseURLInternet";
	
	private String baseURLInternetTarget;
	private String baseURLIntranetTarget;
	private String useridInternetTarget;
	private String useridIntranetTarget;
	private String passwordInternetTarget;
	private String passwordIntranetTarget;

	@Before
	public void before() throws Exception {
		
		baseURLIntranetTarget=null;
		useridIntranetTarget=null;
		passwordIntranetTarget=null;
		
		baseURLInternetTarget=null;
		useridInternetTarget=null;
		passwordInternetTarget=null;
	}
	
	@Test
	public void intranet_set__internet_set() {
		/* prepare */
		baseURLIntranetTarget=BASEURL_INTRANET;
		useridIntranetTarget=USERNAME_INTRANET;
		passwordIntranetTarget=PASSWORD_INTRANET;
		
		baseURLInternetTarget=BASEURL_INTERNET;
		useridInternetTarget=USERNAME_INTERNET;
		passwordInternetTarget=PASSWORD_INTERNET;
		
		/* test */
		expectCanScan(TargetType.INTERNET);
		expectCanScan(TargetType.INTRANET);
	}
	
	@Test
	public void intranet_set__internet_set__but_pwds_missing() {
		/* prepare */
		baseURLIntranetTarget=BASEURL_INTRANET;
		useridIntranetTarget=USERNAME_INTRANET;
		passwordIntranetTarget=null;
		
		baseURLInternetTarget=BASEURL_INTERNET;
		useridInternetTarget=USERNAME_INTERNET;
		passwordInternetTarget=null;
		
		/* test */
		expectCannotScan(TargetType.INTERNET);
		expectCannotScan(TargetType.INTRANET);
	}
	
	@Test
	public void intranet_set__internet_set__but_userids_missing() {
		/* prepare */
		baseURLIntranetTarget=BASEURL_INTRANET;
		useridIntranetTarget=null;
		passwordIntranetTarget=PASSWORD_INTRANET;
		
		baseURLInternetTarget=BASEURL_INTERNET;
		useridInternetTarget=null;
		passwordInternetTarget=PASSWORD_INTERNET;
		
		
		/* test */
		expectCannotScan(TargetType.INTERNET);
		expectCannotScan(TargetType.INTRANET);
	}
	
	@Test
	public void intranet_set__internet_set__but_baseURLs_missing() {
		/* prepare */
		baseURLIntranetTarget=null;
		useridIntranetTarget=USERNAME_INTRANET;
		passwordIntranetTarget=PASSWORD_INTRANET;
		
		baseURLInternetTarget=null;
		useridInternetTarget=USERNAME_INTERNET;
		passwordInternetTarget=PASSWORD_INTERNET;
		
		
		/* test */
		expectCannotScan(TargetType.INTERNET);
		expectCannotScan(TargetType.INTRANET);
	}
	
	
	
	@Test
	public void intranet_not_set__internet_not_set__internet_is_not_scannable_and_not_internet() {
		/* prepare - not necessary, everything null*/
		
		/* test */
		expectCannotScan(TargetType.INTERNET);
		expectCannotScan(TargetType.INTRANET);
	}
	
	@Test
	public void intranet_set__internet_not_set__intranet_is_scannable_but_not_internet() {
		/* prepare */
		baseURLIntranetTarget=BASEURL_INTRANET;
		useridIntranetTarget=USERNAME_INTRANET;
		passwordIntranetTarget=PASSWORD_INTRANET;
		
		/* test */
		expectCanScan(TargetType.INTRANET);
		expectCannotScan(TargetType.INTERNET);
	}
	

	@Test
	public void intranet_not_set__internet_set__intranet_is_not_scannable_but_internet() {
		/* prepare */
		baseURLInternetTarget=BASEURL_INTERNET;
		useridInternetTarget=USERNAME_INTERNET;
		passwordInternetTarget=PASSWORD_INTERNET;
		
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
		TestMultiInstallSetup toTest = new TestMultiInstallSetup();
		
		/* test */
		boolean ableToScan = toTest.isAbleToScan(type);
		if (expectedCan) {
			assertTrue("Expected to be able to scan "+type.name()+" but is NOT!", ableToScan);
		}else {
			assertFalse("Expected NOT to be able to scan "+type.name()+" but is!", ableToScan);
		}
	}

	private class TestMultiInstallSetup extends AbstractTargetIdentifyingMultiInstallSetup {

		@Override
		protected String getBaseURLWhenInternetTarget() {
			return baseURLInternetTarget;
		}

		@Override
		protected String getBaseURLWhenIntranetTarget() {
			return baseURLIntranetTarget;
		}

		@Override
		protected String getUsernameWhenInternetTarget() {
			return useridInternetTarget;
		}

		@Override
		protected String getUsernameWhenIntranetTarget() {
			return useridIntranetTarget;
		}

		@Override
		protected String getPasswordWhenInternetTarget() {
			return passwordInternetTarget;
		}

		@Override
		protected String getPasswordWhenIntranetTarget() {
			return passwordIntranetTarget;
		}

		@Override
		public boolean isHavingUntrustedCertificate(TargetType target) {
			return false;
		}

	}
}
