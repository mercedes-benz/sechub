// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import java.util.List;

import com.daimler.sechub.adapter.LoginScriptGenerator;
import com.daimler.sechub.adapter.LoginScriptStep;

public class NetsparkerLoginScriptGenerator implements LoginScriptGenerator{

	public String generate(List<LoginScriptStep> steps) {
		StringBuilder sb = new StringBuilder();

		generate(steps, sb);

		return sb.toString();
	}

	private void generate(List<LoginScriptStep> steps, StringBuilder sb) {
		if (steps == null) {
			return;
		}
		for (LoginScriptStep step : steps) {
			generate(step, sb);
		}

	}

	private void generate(LoginScriptStep step, StringBuilder sb) {
		if (step == null) {
			return;
		}
		if (step.isClick()) {
			sb.append("ns.auth.clickByQuery('").append(step.getSelector()).append("',2000);\n");
		} else if (step.isInput()) {
			sb.append("ns.auth.setValueByQuery('").append(step.getSelector()).append("','").append(step.getValue()).append("');\n");
		}else if (step.isUserName()) {
			sb.append("ns.auth.setValueByQuery('").append(step.getSelector()).append("',username);\n");
		}else if (step.isPassword()) {
			sb.append("ns.auth.setValueByQuery('").append(step.getSelector()).append("',password);\n");
		}

	}

}
