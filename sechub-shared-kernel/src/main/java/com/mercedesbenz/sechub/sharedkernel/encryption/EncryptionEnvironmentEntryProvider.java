package com.mercedesbenz.sechub.sharedkernel.encryption;

public interface EncryptionEnvironmentEntryProvider {

    /**
     * Resolves value of an environment variable, it is assumed that the value is
     * base 64 encoded
     *
     * @param environmentVariableName
     * @return environment value
     */
    String getBase64EncodedEnvironmentEntry(String environmentVariableName);

}
