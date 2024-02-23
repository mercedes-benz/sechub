package com.mercedesbenz.sechub.sharedkernel.logging;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthUserExtraction {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthUserExtraction.class);

    private static final String BASIC_LOWERCASED = "basic";
    private static final String BASIC_PREFIX_WITH_SPACE = BASIC_LOWERCASED + " ";
    private static final int MIN_AUTH_HEADER_LENGTH = BASIC_PREFIX_WITH_SPACE.length() + 2;

    public String extractUserFromAuthHeader(String basicAuth) {

        try {
            if (basicAuth == null) {
                return "info.no-basic-auth-defined";
            }

            String lowerCased = basicAuth.toLowerCase();
            if (!lowerCased.startsWith(BASIC_LOWERCASED)) {
                return "error.unsupported-format";
            }

            if (basicAuth.length() < MIN_AUTH_HEADER_LENGTH) {
                return "error.too-small";
            }

            String base64 = basicAuth.substring(BASIC_PREFIX_WITH_SPACE.length());

            return decodeUserFromBase64(base64);

        } catch (RuntimeException e) {
            LOG.error("Extraction failed, will return failure message instead", e);
            return "error.extraction-failed-with-exception";
        }
    }

    String decodeUserFromBase64(String base64) {

        byte[] decoded = Base64.getDecoder().decode(base64);
        String decodedString = new String(decoded);

        String[] splitted = decodedString.split(":");
        if (splitted.length != 2) {
            return "error.colon-count-wrong:" + (splitted.length - 1);
        }
        String userName = splitted[0];
        return userName;
    }

}
