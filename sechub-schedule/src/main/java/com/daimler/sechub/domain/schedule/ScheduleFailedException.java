// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

public class ScheduleFailedException extends RuntimeException {

	private static final long serialVersionUID = -6228152266345847909L;

	public ScheduleFailedException(Exception cause) {
		super(cause);
	}

}
