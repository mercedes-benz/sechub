// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.*;

import com.fasterxml.jackson.annotation.JsonFormat;

public enum EncodingType {

    @JsonFormat(with = ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    AUTODETECT,

    @JsonFormat(with = ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    HEX,

    @JsonFormat(with = ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    BASE32,

    @JsonFormat(with = ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    BASE64,

    @JsonFormat(with = ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    PLAIN,

    ;

}
