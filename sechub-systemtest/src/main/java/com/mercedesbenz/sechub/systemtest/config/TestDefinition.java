package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class TestDefinition extends AbstractDefinition {

    private String name;
    private List<ExecutionStepDefinition> prepare = new ArrayList<>();
    private List<TestAssertDefinition> _assert = new ArrayList<>();
    private TestExecutionDefinition execute = new TestExecutionDefinition();

    public void setName(String id) {
        this.name = id;
    }

    public String getName() {
        return name;
    }

    public List<ExecutionStepDefinition> getPrepare() {
        return prepare;
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
