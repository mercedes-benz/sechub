// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.List;

public interface LoginScriptGenerator {

	public String generate(List<LoginScriptAction> actions);
}
