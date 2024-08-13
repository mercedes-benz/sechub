// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;

public class SecHubEncryptionStatusMessageDataProvider implements MessageDataProvider<SecHubDomainEncryptionStatus> {

    @Override
    public SecHubDomainEncryptionStatus get(String json) {
        if (json == null) {
            return null;
        }
        return SecHubDomainEncryptionStatus.fromString(json);
    }

    @Override
    public String getString(SecHubDomainEncryptionStatus data) {
        if (data == null) {
            return null;
        }
        return data.toJSON();
    }

}
