// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model.login;

import java.util.Optional;

public class FormLoginConfiguration {

    public static final String PROPERTY_SCRIPT = "script";

    Optional<Script> script = Optional.empty();

    public Optional<Script> getScript() {
        return script;
    }
}