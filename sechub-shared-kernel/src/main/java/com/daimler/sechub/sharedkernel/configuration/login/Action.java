// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.Optional;

import com.daimler.sechub.adapter.ActionType;
import com.daimler.sechub.adapter.SecHubTimeUnit;

public class Action {

    ActionType type;

    Optional<String> selector = Optional.empty();

    Optional<String> value = Optional.empty();

    Optional<String> description = Optional.empty();

    Optional<SecHubTimeUnit> unit = Optional.empty();

    public ActionType getType() {
        return type;
    }

    public Optional<String> getSelector() {
        return selector;
    }

    public Optional<String> getValue() {
        return value;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<SecHubTimeUnit> getUnit() {
        return unit;
    }
}
