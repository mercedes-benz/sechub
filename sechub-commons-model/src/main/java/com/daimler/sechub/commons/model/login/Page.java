// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model.login;

import java.util.List;
import java.util.Optional;

public class Page {
    protected Optional<List<Action>> actions = Optional.empty();

    public Optional<List<Action>> getActions() {
        return actions;
    }
}
