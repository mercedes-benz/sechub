package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.Optional;

public class ScriptEntry {

	private String step;

	private Optional<String> selector = Optional.empty();

	private Optional<String> value = Optional.empty();

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
