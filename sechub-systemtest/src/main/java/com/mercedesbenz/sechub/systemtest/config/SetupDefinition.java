// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

public class SetupDefinition extends AbstractDefinition {

    private Optional<LocalSetupDefinition> local = Optional.ofNullable(null);
    private Optional<RemoteSetupDefinition> remote = Optional.ofNullable(null);

    public Optional<LocalSetupDefinition> getLocal() {
        return local;
    }

    public void setLocal(Optional<LocalSetupDefinition> local) {
        this.local = local;
    }

    public Optional<RemoteSetupDefinition> getRemote() {
        return remote;
    }

    public void setRemote(Optional<RemoteSetupDefinition> remote) {
        this.remote = remote;
    }
}
