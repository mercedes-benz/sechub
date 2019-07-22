// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

public enum Severity {
	
	INFO, // like faraday: info

	UNCLASSIFIED, // like faraday: unclassfiied

	LOW, // like faraday: low

	MEDIUM, // like faraday: low

	HIGH, // like faraday: high

	CRITICAL,;

	/**
	 * Returns severity or <code>null</code> for given string.
	 * @param string value of the enumeration (is not case sensitive)
	 * @return
	 */
	public static Severity fromString(String string) {
		if (string==null) {
			return null;
		}
		String upperCased=string.toUpperCase();
		for (Severity severity: Severity.values()) {
			if (severity.name().equals(upperCased)) {
				return severity;
			}
		}
		return null;
	}

}
