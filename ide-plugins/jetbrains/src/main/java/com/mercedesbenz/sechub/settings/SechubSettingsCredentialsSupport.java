// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

public class SechubSettingsCredentialsSupport {

    private final String SECHUB_CREDENTIALS_KEY = "SECHUB_CREDENTIALS_KEY";

    public Credentials retrieveCredentials() {
        CredentialAttributes attributes = createCredentialAttributes();
        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        return passwordSafe.get(attributes);
    }

    public void storeCredentials(Credentials credentials) {
        CredentialAttributes attributes = createCredentialAttributes();
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    private CredentialAttributes createCredentialAttributes() {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("SecHub", SECHUB_CREDENTIALS_KEY));
    }
}
