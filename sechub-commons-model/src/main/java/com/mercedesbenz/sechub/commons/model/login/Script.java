// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Script {
    protected Optional<List<Page>> pages = Optional.empty();

    public Optional<List<Page>> getPages() {
        return pages;
    }
}
