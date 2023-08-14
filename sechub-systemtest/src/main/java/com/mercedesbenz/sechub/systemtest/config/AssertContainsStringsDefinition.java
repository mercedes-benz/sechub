// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class AssertContainsStringsDefinition extends AbstractDefinition {

    private List<String> values = new ArrayList<>();

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> strings) {
        this.values = strings;
    }
}
