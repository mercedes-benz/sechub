// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

/*
 * If this needs to get changed, make sure to change
 * com.mercedesbenz.sechub.sereco.importer.SarifImporterKeys accordingly
 */
public enum SarifImporterKeys {

    SECRETSCAN_SERECO_SEVERITY("secretscan.sereco.severity", "The key for the sereco severity which is more precise than the SARIF Level enum."),

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
