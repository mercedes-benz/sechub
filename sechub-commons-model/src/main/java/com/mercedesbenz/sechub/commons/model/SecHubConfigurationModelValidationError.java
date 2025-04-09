// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public enum SecHubConfigurationModelValidationError {

    MODEL_NULL("Model not defined."),

    API_VERSION_NULL("Api version is missing."),

    API_VERSION_NOT_SUPPORTED("Api version is not supported."),

    SECHUB_CONFIGURATION_TOO_LARGE("The provided SecHub scan configuration JSON is too large."),

    DATA_CONFIG_OBJECT_NAME_IS_NULL("One data configuration object has `null` as defined named which is not acceptable."),

    DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_SHORT("Data configuration object name length is too small."),

    DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_LONG("Data configuration object name length is too big."),

    DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE("Data configuration object name is not unique."),

    CONTAINS_NO_SCAN_CONFIGURATION("Configuration does not contain any scan option. Unable to start scan!"),

    INFRA_SCAN_HAS_NO_URIS_OR_IPS_DEFINED("There are no URIs or IPs defined for infra scan, so cannot start scan!"),

    WEB_SCAN_HAS_NO_URL_DEFINED("There are no URLs or URIs defined for web scan. Cannot start scan!"),

    WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA("The URL for web scan has an unsupported schema!"),

    WEB_SCAN_NO_HEADER_NAME_DEFINED("The name for a HTTP header is not defined!"),

    WEB_SCAN_NO_HEADER_VALUE_DEFINED("The value for a HTTP header is not defined!"),

    WEB_SCAN_MULTIPLE_HEADER_VALUES_DEFINED(
            "The HTTP header has multiple values defined! Use either a file reference or a directly specified value but not both!"),

    WEB_SCAN_INCLUDE_INVALID("The value of an include is invalid!"),

    WEB_SCAN_EXCLUDE_INVALID("The value of an exclude is invalid!"),

    WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_IS_NOT_A_VALID_URL("The URL for a HTTP header is not a valid URL!"),

    WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_DOES_NOT_CONTAIN_TARGET_URL("The URL for a HTTP header does not contain the base URL that shall be scanned!"),

    WEB_SCAN_NON_UNIQUE_HEADER_CONFIGURATION("The webscan config contains header configurations that default to the same URL scope!"),

    WEB_SCAN_LOGIN_TOTP_CONFIGURATION_INVALID("The TOTP configuration inside the webscan config login section is invalid!"),

    WEB_SCAN_LOGOUT_CONFIGURATION_INVALID("The logout configuration inside the webscan config is invalid!"),

    WEB_SCAN_LOGIN_VERIFICATION_CONFIGURATION_INVALID("The verification configuration inside the webscan config login section is invalid!"),

    REFERENCED_DATA_CONFIG_OBJECT_NAME_NOT_EXISTING("The referenced config object name was not found in object!"),

    NO_DATA_CONFIG_SPECIFIED_FOR_SCAN("No data config was specified for the scan!"),

    DATA_CONFIG_OBJECT_NAME_CONTAINS_ILLEGAL_CHARACTERS("Data configuration object name contains illegal characters!"),

    NO_PUBLIC_SCAN_TYPES_DETECTED("No public scan types can be detected."),

    NO_MODULE_GROUP_DETECTED("No module group detected."),

    MULTIPLE_MODULE_GROUPS_DETECTED("Multiple module groups detected."),

    METADATA_LABEL_KEY_TOO_SHORT("Metadata label key length is too short."),

    METADATA_LABEL_KEY_TOO_LONG("Meta data label key length is too long."),

    METADATA_LABEL_VALUE_TOO_LONG("Meta data label value length is too long."),

    METADATA_TOO_MANY_LABELS("Too many meta data labels defined!"),

    METADATA_LABEL_KEY_CONTAINS_ILLEGAL_CHARACTERS("Meta data label key contains illegal characters."),

    REMOTE_DATA_CONFIGURATION_ONLY_FOR_ONE_SOURCE_OR_BINARY("Remote data configuration is only allowed for ONE source or for ONE binary."),

    REMOTE_DATA_MIXED_WITH_FILESYSTEM_NOT_ALLOWED("Remote data configuration is not allowed to be mixed with filesystem."),

    REMOTE_DATA_CONFIGURATION_LOCATION_NOT_DEFINED("Remote data configuration location is not defined."),

    REMOTE_DATA_CONFIGURATION_USER_NOT_DEFINED("Remote data configuration credentials: no user is defined."),

    REMOTE_DATA_CONFIGURATION_USER_NAME_NOT_DEFINED("Remote data configuration credentials: user name is not defined."),

    REMOTE_DATA_CONFIGURATION_USER_PASSWORD_NOT_DEFINED("Remote data configuration credentials: user password is not defined."),

    RESERVED_REFERENCE_ID_MUST_NOT_BE_USED_IN_DATA_SECTION("An internal, reserved reference id may not be manually defined inside a data section."),

    ILLEGAL_SOURCE_DATA_REFERENCE("Illegal source data reference."),

    ILLEGAL_BINARY_DATA_REFERENCE("Illegal binary data reference."),

    ;

    private String defaultMessage;

    private SecHubConfigurationModelValidationError(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
