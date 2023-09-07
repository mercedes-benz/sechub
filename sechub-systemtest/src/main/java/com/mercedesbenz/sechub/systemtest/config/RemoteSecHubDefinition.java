// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

public class RemoteSecHubDefinition extends AbstractSecHubDefinition {

    private CredentialsDefinition user = new CredentialsDefinition();

    public CredentialsDefinition getUser() {
        return user;
    }
}
