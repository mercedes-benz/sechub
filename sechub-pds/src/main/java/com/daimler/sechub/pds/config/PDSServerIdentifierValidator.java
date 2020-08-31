// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.config;

import org.springframework.stereotype.Component;

@Component
public class PDSServerIdentifierValidator {

    /**
     * Creates error message when validation failed
     * 
     * @param serverId
     * @return failure message or <code>null</code> when valid
     */
    public String createValidationErrorMessage(String serverId) {
        if (serverId == null || serverId.trim().isEmpty()) {
            return "server identifier not set!";
        }
        if (serverId.length() > 30) {
            return "server identifier length too big:" + serverId.length() + ", allowed is only 30";
        }
        for (char c : serverId.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                continue;
            }
            if (Character.isDigit(c)) {
                continue;
            }
            if (c == '_') {
                continue;
            }
            return "unexpected char inside server identifier found:" + c + ", allowed is only [a-zA-Z0-9_]";
        }
        return null;
    }

}
