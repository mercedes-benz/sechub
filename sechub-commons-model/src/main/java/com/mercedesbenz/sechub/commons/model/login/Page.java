// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {
    protected Optional<List<Action>> actions = Optional.empty();

    public Optional<List<Action>> getActions() {
        return actions;
    }
}
