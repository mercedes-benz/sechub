// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

public enum ConfigurationSetup {

	ADMIN_USERNAME ("sechub.developertools.admin.userid"),
	ADMIN_APITOKEN ("sechub.developertools.admin.apitoken"),
	ADMIN_SERVER ("sechub.developertools.admin.server"),
	ADMIN_SERVER_PORT ("sechub.developertools.admin.serverport"),
	ENABLE_INTEGRATION_TESTSERVER_MENU ("sechub.developertools.admin.integrationtestserver"),
	;

	private String id;

	private ConfigurationSetup(String id) {
		this.id=id;
	}

	public String getId() {
		return id;
	}

	public static boolean isIntegrationTestServerMenuEnabled() {
		return Boolean.getBoolean(ConfigurationSetup.ENABLE_INTEGRATION_TESTSERVER_MENU.getId());
	}

	public static String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("Use following system properties:\n");
		for (ConfigurationSetup setup: values()) {
			sb.append("-D");
			sb.append(setup.id);
			sb.append("=");
			String val = System.getProperty(setup.id);
			if (val!=null && !val.isEmpty()) {
				val="****";
			}
			sb.append(val);
			sb.append("\n");
		}
		return sb.toString();
	}
}
