// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormLoginConfiguration {

    public static final String PROPERTY_SCRIPT = "script";

    Optional<Script> script = Optional.empty();

    public Optional<Script> getScript() {
        return script;
    }
}