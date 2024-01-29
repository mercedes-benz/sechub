// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class TestDefinition extends AbstractDefinition {

    private String name;
    private List<ExecutionStepDefinition> prepare = new ArrayList<>();
    private List<TestAssertDefinition> _assert = new ArrayList<>();
    private TestExecutionDefinition execute = new TestExecutionDefinition();
    private List<ExecutionStepDefinition> cleanup = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ExecutionStepDefinition> getPrepare() {
        return prepare;
    }

    public List<ExecutionStepDefinition> getCleanup() {
        return cleanup;
    }

    public TestExecutionDefinition getExecute() {
        return execute;
    }

    public void setExecute(TestExecutionDefinition execute) {
        this.execute = execute;
    }

    public List<TestAssertDefinition> getAssert() {
        return _assert;
    }

}
