// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MockEmailEntry{
	public String from;
	public String to;
	public String subject;
	public String text;

	@Override
	public String toString() {
		return "MockEmailEntry: [subject=" + subject + ", from=" + from + ", to=" + to + "]";
	}

	public String fullToString() {
		return "MockEmail '" + subject + "'\nfrom=" + from + "\nto=" + to +"\n\n" + text + "]\n";
	}

}