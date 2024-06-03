// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;

public abstract class AbstractPrepareToolContext implements PrepareToolContext {

    private static final String UPLOAD_DIRECTORY_NAME = "upload";

    private String location;
    protected Path uploadDirectory;

    private Map<String, SealedObject> credentialMap = new HashMap<>();

    public void setLocation(String location) {
        this.location = location;
    }

    public void init(Path workingDirectory) {
        if (workingDirectory == null) {
            throw new IllegalArgumentException("Upload directory may not be null!");
        }
        this.uploadDirectory = workingDirectory.resolve(UPLOAD_DIRECTORY_NAME);
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Path getUploadDirectory() {
        return uploadDirectory;
    }

    public String getUnsealedUsername() {
        return CryptoAccess.CRYPTO_STRING.unseal(credentialMap.get(PDS_PREPARE_CREDENTIAL_USERNAME));
    }

    public String getUnsealedPassword() {
        return CryptoAccess.CRYPTO_STRING.unseal(credentialMap.get(PDS_PREPARE_CREDENTIAL_PASSWORD));
    }

    public boolean hasCredentials() {
        return !credentialMap.isEmpty();
    }

    /*
     * TODO Albert Tregnaghi, 2024-06-06: the user data is still in memory - means
     * context does seal, but sensitive data is still plain inside memory (caller
     * side)! Must be fixed with config encryption!
     */
    public void setSealedCredentials(SecHubRemoteCredentialUserData user) {
        setSealedCredentials(user.getName(), user.getPassword());
    }

    public void setSealedCredentials(String username, String password) {

        SealedObject sealedUsername = CryptoAccess.CRYPTO_STRING.seal(username);
        credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, sealedUsername);

        SealedObject sealedPassword = CryptoAccess.CRYPTO_STRING.seal(password);
        credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, sealedPassword);
    }
}
