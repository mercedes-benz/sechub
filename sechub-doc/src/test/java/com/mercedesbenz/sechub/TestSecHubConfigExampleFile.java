// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

public enum TestSecHubConfigExampleFile {

    DATA_SECTION_EXAMPLE_1("src/docs/asciidoc/documents/shared/configuration/sechub_config_data_section_general_example1.json"),

    DATA_SECTION_EXAMPLE_2("src/docs/asciidoc/documents/shared/configuration/sechub_config_data_section_general_example2.json"),

    SOURCESCAN_FILESYSTEM("src/docs/asciidoc/documents/shared/configuration/sechub_config_example1_sourcescan_filesystem.json"),

    LICENSESCAN_WITH_SOURCES_DATA_REFERENCE(
            "src/docs/asciidoc/documents/shared/configuration/sechub_config_example10_license_scan_with_sources_data_reference.json"),

    LICENSESCAN_AND_CODESCAN_WITH_SOURCES_DATA_REFERENCE(
            "src/docs/asciidoc/documents/shared/configuration/sechub_config_example11_license_scan_and_code_scan_with_sources_data_reference.json"),

    WEBSCAN_ANONYMOUS("src/docs/asciidoc/documents/shared/configuration/sechub_config_example2_webscan_anonymous.json"),

    WEBSCAN_BASIC_AUTH("src/docs/asciidoc/documents/shared/configuration/sechub_config_example3_webscan_login_basicauth.json"),

    WEBSCAN_FORM_BASED_SCRIPT_AUTH("src/docs/asciidoc/documents/shared/configuration/sechub_config_example5_webscan_login_from_sript.json"),

    INFRASCAN_IP("src/docs/asciidoc/documents/shared/configuration/sechub_config_example6_infrascan_ip.json"),

    INFRASCAN_URI("src/docs/asciidoc/documents/shared/configuration/sechub_config_example7_infrascan_uri.json"),

    WEBSCAN_OPENAPI_WITH_DATA_REFERENCE("src/docs/asciidoc/documents/shared/configuration/sechub_config_example8_web_scan_openapi_with_data_reference.json"),

    LICENSESCAN_WITH_BINARIES_DATA_REFERENCE(
            "src/docs/asciidoc/documents/shared/configuration/sechub_config_example9_license_scan_with_binaries_data_reference.json"),

    WEBSCAN_HEADER_SCAN("src/docs/asciidoc/documents/shared/configuration/sechub_config_example15_web_scan_header.json"),

    WEBSCAN_HEADER_TO_IDENTIFY_SECHUB_REQUESTS(
            "src/docs/asciidoc/documents/shared/configuration/sechub_config_example23_headers_to_identify_webscans_in_logs.json"),

    WEBSCAN_HEADER_FROM_DATA_REFERENCE("src/docs/asciidoc/documents/shared/configuration/sechub_config_example17_web_scan_header_value_from_data_section.json"),

    WEBSCAN_CLIENT_CERTIFICATE("src/docs/asciidoc/documents/shared/configuration/sechub_config_example4_webscan_login_clientcertificate.json"),

    WEBSCAN_CLIENT_CERTIFICATE_WITH_OPENAPI(
            "src/docs/asciidoc/documents/shared/configuration/sechub_config_example16_webscan_client_certificate_with_openAPI.json"),

    WEBSCAN_FORM_BASED_SCRIPT_AUTH_WITH_TOTP("src/docs/asciidoc/documents/shared/configuration/sechub_config_example21_webscan_login_form_with_totp.json"),

    WEBSCAN_LOGOUT_CONFIGURATION("src/docs/asciidoc/documents/shared/configuration/sechub_config_example25_logout_config.json"),

    WEBSCAN_LOGIN_VALIDATION("src/docs/asciidoc/documents/shared/configuration/sechub_config_example24_webscan_login_form_with_verification.json"),

    ;

    private String path;

    private TestSecHubConfigExampleFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
