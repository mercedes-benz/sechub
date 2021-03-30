// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.Optional;

public class FormLoginConfiguration {

    public static final String PROPERTY_SCRIPT = "script";
    
	Optional<AutoDetectUserLoginConfiguration> autodetect = Optional.empty();
	Optional<Script> script = Optional.empty();

	public Optional<AutoDetectUserLoginConfiguration> getAutodetect() {
		return autodetect;
	}

	public Optional<Script> getScript() {
		return script;
	}
}