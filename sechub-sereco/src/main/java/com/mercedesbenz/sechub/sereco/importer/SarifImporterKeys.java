// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

/*
 * If this needs to get changed, make sure to change
 * com.mercedesbenz.sechub.wrapper.secret.validator.support.SarifImporterKeys accordingly
 */
public enum SarifImporterKeys {

    SECRETSCAN_SECHUB_SEVERITY("secretscan.sechub.severity", "The key for the sechub severity which is more precise than the SARIF Level enum."),

    SECRETSCAN_VALIDATED_BY_URL("secretscan.validated.by.url", "The key for the URL the secret was validated with."),

    ;

    private String key;
    private String description;

    private SarifImporterKeys(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
