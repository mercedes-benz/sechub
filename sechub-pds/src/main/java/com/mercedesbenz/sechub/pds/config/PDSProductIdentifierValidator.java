// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

@Component
public class PDSProductIdentifierValidator {

    private static final int PDS_PRODUCT_IDENTIFIER_MAX_LENGTH = 50;

    /**
     * Creates error message when validation failed
     *
     * @param productId
     * @return failure message or <code>null</code> when valid
     */
    public String createValidationErrorMessage(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return "product identifier not set!";
        }
        if (productId.length() > PDS_PRODUCT_IDENTIFIER_MAX_LENGTH) {
            return "product identifier length too big:" + productId.length() + ", allowed is only " + PDS_PRODUCT_IDENTIFIER_MAX_LENGTH;
        }
        if (!SimpleStringUtils.hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(productId, '_')) {
            return "unexpected char inside product identifier found, allowed is only [a-zA-Z0-9_]";
        }

        return null;
    }

}
