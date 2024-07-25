// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.nio.file.Path;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;

public abstract class AbstractPrepareToolContext implements PrepareToolContext {

    private SealedObject sealedUsername;
    private SealedObject sealedPassword;

    private static final String UPLOAD_DIRECTORY_NAME = "upload";

    private String location;
    protected Path uploadDirectory;

    public void init(Path workingDirectory) {
        if (workingDirectory == null) {
            throw new IllegalArgumentException("Upload directory may not be null!");
        }
        this.uploadDirectory = workingDirectory.resolve(UPLOAD_DIRECTORY_NAME);
    }

    public void setLocation(String location) {
        this.location = location;
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
        if (sealedUsername == null) {
            return null;
        }
        return CryptoAccess.CRYPTO_STRING.unseal(sealedUsername);
    }

    public String getUnsealedPassword() {
        if (sealedPassword == null) {
            return null;
        }
        return CryptoAccess.CRYPTO_STRING.unseal(sealedPassword);
    }

    public boolean hasCredentials() {
        return sealedUsername != null && sealedPassword != null;
    }

    public void setSealedCredentials(SecHubRemoteCredentialUserData user) {
        if (user == null) {
            setSealedCredentials(null, null);
        } else {
            setSealedCredentials(user.getName(), user.getPassword());
        }
    }

    public void setSealedCredentials(String username, String password) {

        if (username == null) {
            sealedUsername = null;
        } else {
            sealedUsername = CryptoAccess.CRYPTO_STRING.seal(username);
        }

        if (password == null) {
            sealedPassword = null;
        } else {
            sealedPassword = CryptoAccess.CRYPTO_STRING.seal(password);
        }
    }
}
