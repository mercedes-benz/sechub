// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

/**
 * The type id is used for logging. So do NOT change the type id. We use the
 * type id to have possibility to refactor namings in code and avoid older logs
 * no longer be valid
 *
 * @author Albert Tregnaghi
 *
 */
public enum SecurityLogType {

	UNKNOWN("UNKNOWN"),

	POTENTIAL_INTRUSION("POTENTIAL INTRUSION"),
	;

	private String typeId;

	private SecurityLogType(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeId() {
		return typeId;
	}
}
