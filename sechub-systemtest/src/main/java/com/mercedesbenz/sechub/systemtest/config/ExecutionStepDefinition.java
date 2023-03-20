package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

/**
 * Represents a step definition which really executes/does something at runtime.
 * For example: execute a script
 *
 * @author Albert Tregnaghi
 *
 */
public class ExecutionStepDefinition extends AbstractDefinition {

    private Optional<ScriptDefinition> script = Optional.ofNullable(null);

    public Optional<ScriptDefinition> getScript() {
        return script;
    }

    public void setScript(Optional<ScriptDefinition> script) {
        this.script = script;
    }

}
