// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

public class RemoteSetupDefinition extends AbstractDefinition {

    private RemoteSecHubDefinition secHub = new RemoteSecHubDefinition();

    public RemoteSecHubDefinition getSecHub() {
        return secHub;
    }

}
