// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile("!" + Profiles.INTEGRATIONTEST)
public class DefaultEncryptionEnvironmentEntryProvider implements EncryptionEnvironmentEntryProvider {

    @Override
    public String getBase64EncodedEnvironmentEntry(String environmentVariableName) {
        return System.getenv(environmentVariableName);
    }

}
