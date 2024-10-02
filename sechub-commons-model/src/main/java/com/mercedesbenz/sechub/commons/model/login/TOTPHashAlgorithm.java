// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

public enum TOTPHashAlgorithm {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({ "HmacSHA1" })
    HMAC_SHA1("HmacSHA1"),

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({ "HmacSHA256" })
    HMAC_SHA256("HmacSHA256"),

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({ "HmacSHA512" })
    HMAC_SHA512("HmacSHA512"),

    ;

    private final String name;

    private TOTPHashAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
