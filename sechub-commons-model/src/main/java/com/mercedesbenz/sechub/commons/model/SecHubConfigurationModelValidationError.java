// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public enum SecHubConfigurationModelValidationError {

    MODEL_NULL("Model not defined."),

    API_VERSION_NULL("Api version is missing."),

    API_VERSION_NOT_SUPPORTED("Api version is not supported."),

    DATA_CONFIG_OBJECT_NAME_IS_NULL("One data configuration object has `null` as defined named which is not acceptable."),

    DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_SHORT("Data configuration object name length is too small."),

    DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_LONG("Data configuration object name length is too big."),

    DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE("Data configuration object name is not unique."),

    CONTAINS_NO_SCAN_CONFIGURATION("Configuration does not contain any scan option. Unable to start scan!"),

    INFRA_SCAN_HAS_NO_URIS_OR_IPS_DEFINED("There are no URIs or IPs defined for infra scan, so cannot start scan!"),

    WEB_SCAN_HAS_NO_URL_DEFINED("There are no URLs or URIs defined for web scan. Cannot start scan!"),

    WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA("The URL for web scan has an unsupported schema!"),

    REFERENCED_DATA_CONFIG_OBJECT_NAME_NOT_EXISTING("The referenced config object name was not found in object!"),
    
    NO_DATA_CONFIG_SPECIFIED_FOR_SCAN("No data config was specified for the scan!"),

    DATA_CONFIG_OBJECT_NAME_CONTAINS_ILLEGAL_CHARACTERS("Data configuration object name contains illegal characters!"),

    ;

    private String defaultMessage;

    private SecHubConfigurationModelValidationError(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
