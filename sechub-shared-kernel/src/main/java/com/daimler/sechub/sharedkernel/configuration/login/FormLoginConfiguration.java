package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.List;
import java.util.Optional;

public class FormLoginConfiguration {

	private Optional<AutoDetectUserLoginConfiguration> autodetect = Optional.empty();
	private Optional<List<ScriptEntry>> script = Optional.empty();

	public Optional<AutoDetectUserLoginConfiguration> getAutodetect() {
		return autodetect;
	}

	public Optional<List<ScriptEntry>> getScript() {
		return script;
	}


}