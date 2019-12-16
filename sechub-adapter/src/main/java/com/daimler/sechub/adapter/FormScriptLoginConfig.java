// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.ArrayList;
import java.util.List;

public class FormScriptLoginConfig extends AbstractLoginConfig{

	private List<LoginScriptStep> steps = new ArrayList<>();

	public List<LoginScriptStep> getSteps() {
		return steps;
	}

	public String getUserName() {
		for (LoginScriptStep step : steps) {
			if (step.isUserName()) {
				return step.getValue();
			}
		}
		return "<unknown-user>";
	}

	public String getPassword() {
		for (LoginScriptStep step : steps) {
			if (step.isPassword()) {
				return step.getValue();
			}
		}
		return "<unknown-pwd>";
	}
}
