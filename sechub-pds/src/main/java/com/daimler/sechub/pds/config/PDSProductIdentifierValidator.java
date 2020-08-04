// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.config;

import org.springframework.stereotype.Component;

@Component
public class PDSProductIdentifierValidator {

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
        if (productId.length() > 30) {
            return "product identifier length too big:" + productId.length() + ", allowed is only 30";
        }
        for (char c : productId.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                continue;
            }
            if (Character.isDigit(c)) {
                continue;
            }
            if (c == '_') {
                continue;
            }
            return "unexpected char inside product identifier found:" + c + ", allowed is only [a-zA-Z0-9_]";
        }
        return null;
    }

}
