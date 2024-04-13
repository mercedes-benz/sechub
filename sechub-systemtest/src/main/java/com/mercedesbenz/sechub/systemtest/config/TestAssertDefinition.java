// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

public class TestAssertDefinition extends AbstractDefinition {

    private Optional<AssertSechubResultDefinition> sechubResult = Optional.empty();

    public Optional<AssertSechubResultDefinition> getSechubResult() {
        return sechubResult;
    }

    public void setSechubResult(Optional<AssertSechubResultDefinition> sechubResult) {
        this.sechubResult = sechubResult;
    }

}
