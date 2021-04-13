// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.List;
import java.util.Optional;

public class Script {
    protected Optional<List<Page>> pages = Optional.empty();

    public Optional<List<Page>> getPages() {
        return pages;
    }    
}
