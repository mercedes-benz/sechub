// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import com.fasterxml.jackson.annotation.JsonFormat;

public enum EncodingType {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    AUTODETECT,

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    HEX,

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    BASE32,

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    BASE64,

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    PLAIN,

    ;

}
