// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubEncryptionStatus implements JSONable<SecHubEncryptionStatus> {

    private static SecHubEncryptionStatus CONVERTER = new SecHubEncryptionStatus();

    private String type = "encryptionStatus";

    public String getType() {
        return type;
    }

    private List<SecHubDomainEncryptionStatus> domains = new ArrayList<>();

    public List<SecHubDomainEncryptionStatus> getDomains() {
        return domains;
    }

    @Override
    public Class<SecHubEncryptionStatus> getJSONTargetClass() {
        return SecHubEncryptionStatus.class;
    }

    public static SecHubEncryptionStatus fromString(String statusAsString) {
        return CONVERTER.fromJSON(statusAsString);
    }
}
