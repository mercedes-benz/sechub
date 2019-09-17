// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

public enum ConfigurationSetup {

	SECHUB_ADMIN_USERID(false),

	SECHUB_ADMIN_APITOKEN(false),

	SECHUB_ADMIN_SERVER("sechub.developertools.admin.server",false),

	SECHUB_ADMIN_SERVER_PORT("sechub.developertools.admin.serverport",true),

	SECHUB_ADMIN_SERVER_PROTOCOL("sechub.developertools.admin.serverprotocol",true),

	SECHUB_ENABLE_INTEGRATION_TESTSERVER_MENU("sechub.developertools.admin.integrationtestserver",true),;


	private String systemPropertyid;
	private String environmentEntryId;
	private boolean optional;

	private ConfigurationSetup(boolean optional) {
		this.optional=optional;
		this.systemPropertyid = null;
		this.environmentEntryId = name();
	}

	private ConfigurationSetup(String systemPropertyid, boolean optional) {
		this.optional=optional;
		this.systemPropertyid = systemPropertyid;
		this.environmentEntryId = null;
	}

	public String getEnvironmentEntryId() {
		return environmentEntryId;
	}

	public String getSystemPropertyid() {
		return systemPropertyid;
	}

	public static boolean isIntegrationTestServerMenuEnabled() {
		return Boolean.getBoolean(ConfigurationSetup.SECHUB_ENABLE_INTEGRATION_TESTSERVER_MENU.getSystemPropertyid());
	}

	/**
	 * Resolves string value of configuration and fails when not configured
	 * @return value
	 * @throws IllegalStateException when value not found
	 */
	public String getStringValueOrFail() {
		return getStringValue(null);
	}

	/**
	 * Resolves string value of configuration.
	 * @param defaultValue
	 * @return value or default value
	 * @throws IllegalStateException when value not found and no default value available
	 */
	public String getStringValue(String defaultValue) {
		String value = null;
		if (environmentEntryId != null) {
			value = System.getenv(environmentEntryId);
		}else {
			value = System.getProperty(getSystemPropertyid(), defaultValue);
		}
		assertNotEmpty(value, name());
		return value;
	}

	private void assertNotEmpty(String part, String missing) {
		if (part == null || part.isEmpty()) {
			throw new IllegalStateException(
					"Missing configuration entry:" + missing + ".\nYou have to configure these values:" + ConfigurationSetup.description());
		}

	}

	private static String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("Use following system properties:\n");
		for (ConfigurationSetup setup : values()) {
			if (setup.systemPropertyid == null) {
				continue;
			}
			sb.append("-D");
			sb.append(setup.systemPropertyid);
			sb.append("=");
			String val = System.getProperty(setup.systemPropertyid);
			if (val != null && !val.isEmpty()) {
				val = "**** (already set)";
			}
			sb.append(val);
			if (setup.optional) {
				sb.append(" (optional)");
			}
			sb.append("\n");
		}
		sb.append("\nFor security reasons next parts must be set as environment variables (so not visible in process view):\n");
		for (ConfigurationSetup setup : values()) {
			if (setup.environmentEntryId == null) {
				continue;
			}
			sb.append("  ");
			sb.append(setup.environmentEntryId);
			sb.append("'\n");
		}
		return sb.toString();
	}
}
