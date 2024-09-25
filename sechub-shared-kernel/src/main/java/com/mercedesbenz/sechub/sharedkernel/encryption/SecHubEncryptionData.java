// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubEncryptionData implements JSONable<SecHubEncryptionData> {

    private static final SecHubEncryptionData CONVERTER = new SecHubEncryptionData();

    public static final String PROPERTY_ALGORITHM = "algorithm";
    public static final String PROPERTY_PASSWORD_SOURCETYPE = "passwordSourceType";
    public static final String PROPERTY_PASSWORD_SOURCEDATA = "passwordSourceData";

    private SecHubCipherAlgorithm algorithm;

    private SecHubCipherPasswordSourceType passwordSourceType;

    private String passwordSourceData;

    public SecHubCipherAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SecHubCipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public SecHubCipherPasswordSourceType getPasswordSourceType() {
        return passwordSourceType;
    }

    public void setPasswordSourceType(SecHubCipherPasswordSourceType passwordSourceType) {
        this.passwordSourceType = passwordSourceType;
    }

    public String getPasswordSourceData() {
        return passwordSourceData;
    }

    public void setPasswordSourceData(String passwordSourceData) {
        this.passwordSourceData = passwordSourceData;
    }

    @Override
    public Class<SecHubEncryptionData> getJSONTargetClass() {
        return SecHubEncryptionData.class;
    }

    public static SecHubEncryptionData fromString(String dataAsString) {
        return CONVERTER.fromJSON(dataAsString);
    }

}
