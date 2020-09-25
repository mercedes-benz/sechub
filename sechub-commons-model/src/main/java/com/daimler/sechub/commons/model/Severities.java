// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;
import static com.daimler.sechub.commons.model.Severity.*;

public class Severities {
	
	private Severities() {}
	
	/* @formatter:off */
	private static final  Severity[] ORDERED_FROM_HIGH_TO_LOW = new Severity[] {
			CRITICAL, 
			HIGH, 
			MEDIUM, 
			UNCLASSIFIED, 
			LOW, 
			INFO
		};
	/* @formatter:on */
	
	public static Severity[] getAllOrderedFromHighToLow() {
		return ORDERED_FROM_HIGH_TO_LOW;
	}
}
