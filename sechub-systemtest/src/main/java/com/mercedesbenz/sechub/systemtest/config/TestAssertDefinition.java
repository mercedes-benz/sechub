package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class TestAssertDefinition extends AbstractDefinition {

    private List<AssertSechubResultDefinition> sechubResult = new ArrayList<>();

    public List<AssertSechubResultDefinition> getSechubResult() {
        return sechubResult;
    }

}
