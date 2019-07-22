// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.HashMap;
import java.util.Map;

import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.TestURLBuilder;

/**
 * Test context class. Contains initial data like port, hostname etc.
 *
 * @author Albert Tregnaghi
 *
 */
public class IntegrationTestContext {

	static IntegrationTestContext testContext = new IntegrationTestContext();

	private MockEmailAccess mailAccess = MockEmailAccess.mailAccess();

	private Map<TestUser, TestRestHelper> restHelperMap = new HashMap<>();
	private String hostname = "localhost";
	private int port = TestPortProvider.DEFAULT_INSTANCE.getIntegrationTestServerPort();

	private TestURLBuilder urlBuilder;

	public static IntegrationTestContext get() {
		return testContext;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public TestURLBuilder getUrlBuilder() {
		if (urlBuilder == null) {
			urlBuilder = new TestURLBuilder("https", port, hostname);
		}
		return urlBuilder;
	}

	/**
	 * @return template for super admin
	 */
	public TestRestHelper getTemplateForSuperAdmin() {
		return getRestHelper(TestAPI.SUPER_ADMIN);
	}

	private IntegrationTestContext() {

	}

	public TestRestHelper getSuperAdminRestHelper() {
		return getRestHelper(TestAPI.SUPER_ADMIN);
	}

	public TestRestHelper getRestHelper(TestUser user) {
		return restHelperMap.computeIfAbsent(user, this::createRestHelper);
	}

	private TestRestHelper createRestHelper(TestUser user) {
		return new TestRestHelper(user);
	}

	public MockEmailAccess emailAccess() {
		return mailAccess;
	}

}
