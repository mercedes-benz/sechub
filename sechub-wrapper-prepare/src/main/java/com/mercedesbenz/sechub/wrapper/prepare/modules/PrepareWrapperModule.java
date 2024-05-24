// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_PASSWORD;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_USERNAME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import javax.crypto.SealedObject;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public interface PrepareWrapperModule {

    boolean prepare(PrepareWrapperContext context) throws IOException;

    default void addSealedUserCredentials(SecHubRemoteCredentialUserData user, HashMap<String, SealedObject> credentialMap) {
        SealedObject sealedUsername = CryptoAccess.CRYPTO_STRING.seal(user.getName());
        SealedObject sealedPassword = CryptoAccess.CRYPTO_STRING.seal(user.getPassword());
        credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, sealedUsername);
        credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, sealedPassword);
    }

    default void createDownloadDirectory(Path path) {
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating download directory: " + path.toString(), e);
        }
    }
}
