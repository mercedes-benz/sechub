// SPDX-License-Identifier: MIT
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

    private Optional<ScriptDefinition> script = Optional.empty();
    private Optional<CopyDefinition> copy = Optional.empty();

    public Optional<ScriptDefinition> getScript() {
        return script;
    }

    public Optional<CopyDefinition> getCopy() {
        return copy;
    }

    public void setScript(Optional<ScriptDefinition> script) {
        this.script = script;
    }

    public void setCopy(Optional<CopyDefinition> copy) {
        this.copy = copy;
    }

}
