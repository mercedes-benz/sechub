// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class APITokenGenerator {

    public String generateNewAPIToken() {
        /* UUID uses SecureRandom... */
        String uuidAsString = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(uuidAsString.getBytes());
    }

}
