// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.Optional;

public class ScriptEntry {

	String step;

	Optional<String> selector = Optional.empty();

	Optional<String> value = Optional.empty();

	/**
	 * @return type of step as string (e.g. "input", "click")
	 */
	public String getStep() {
		return step;
	}
	public Optional<String> getSelector() {
		return selector;
	}

	public Optional<String> getValue() {
		return value;
	}

}
