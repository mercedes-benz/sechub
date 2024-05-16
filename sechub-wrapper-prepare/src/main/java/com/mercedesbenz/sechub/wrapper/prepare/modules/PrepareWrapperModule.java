// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.io.IOException;
import java.util.HashMap;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

import javax.crypto.SealedObject;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_PASSWORD;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_USERNAME;

@Service
public interface PrepareWrapperModule {

    public boolean isAbleToPrepare(PrepareWrapperContext context);

    void prepare(PrepareWrapperContext context) throws IOException;

    default boolean isTypeConfigured(String type) {
        return type != null && !type.isBlank();
    }

    default void addSealedUserCredentials(SecHubRemoteCredentialUserData user, HashMap<String, SealedObject> credentialMap) {
        SealedObject sealedUsername = CryptoAccess.CRYPTO_STRING.seal(user.getName());
        SealedObject sealedPassword = CryptoAccess.CRYPTO_STRING.seal(user.getPassword());
        credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, sealedUsername);
        credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, sealedPassword);
    }

    default boolean isMatchingType(String type, String expectedType) {
        return expectedType.equalsIgnoreCase(type);
    }

}
