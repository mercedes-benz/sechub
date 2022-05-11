// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoLicenseSpdx {
    public static String SPDX_JSON_IDENTIFIER = "\"SPDXRef-DOCUMENT\"";
    public static String SPDX_TAG_VALUE_IDENTIFIER = "SPDXRef-DOCUMENT";
    public static String SPDX_RDF_IDENTIFIER = "<rdf:RDF";
    public static String SPDX_YAML_IDENTIFIER = "SPDXID: \"SPDXRef-DOCUMENT\"";

    private String json;
    private String yaml;
    private String rdf;
    private String tagValue;

    /**
     * The SPDX standard defines several document types.
     *
     * This method accepts any document type and sets the correct type.
     *
     * @param spdx
     * @return
     */
    public static SerecoLicenseSpdx of(String spdx) {
        Objects.requireNonNull(spdx, "SPDX cannot be null.");

        boolean isSpdxJson = false;
        boolean isSpdxTagValue = false;
        boolean isSpdxRdf = false;
        boolean isSpdxYaml = false;

        if (spdx == "") {
            throw new IllegalArgumentException("SPDX cannot be empty.");
        }

        SerecoLicenseSpdx spdxLicense = new SerecoLicenseSpdx();
        spdx = spdx.trim();

        if (spdx.startsWith("{") && spdx.contains(SPDX_JSON_IDENTIFIER)) {
            isSpdxJson = true;
            spdxLicense.setJson(spdx);
        }

        if (spdx.contains(SPDX_TAG_VALUE_IDENTIFIER)) {
            isSpdxTagValue = true;
            spdxLicense.setTagValue(spdx);
        }

        if (spdx.contains(SPDX_RDF_IDENTIFIER)) {
            isSpdxRdf = true;
            spdxLicense.setRdf(spdx);
        }

        if (spdx.contains(SPDX_YAML_IDENTIFIER)) {
            isSpdxYaml = true;
            spdxLicense.setYaml(spdx);
        }

        if (!isSpdxJson && !isSpdxTagValue && !isSpdxRdf && !isSpdxYaml) {
            throw new IllegalArgumentException("Not a SPDX document.");
        }

        return spdxLicense;
    }

    private void setJson(String spdx) {
        this.json = spdx;
    }

    private void setYaml(String yaml) {
        this.yaml = yaml;
    }

    private void setRdf(String rdf) {
        this.rdf = rdf;
    }

    private void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    public String getJson() {
        return json;
    }

    public String getYaml() {
        return yaml;
    }

    public String getRdf() {
        return rdf;
    }

    public String getTagValue() {
        return tagValue;
    }

    public boolean hasJson() {
        boolean hasJson = false;

        if (json != null) {
            hasJson = true;
        }

        return hasJson;
    }

    public boolean hasTagValue() {
        boolean hasTagValue = false;

        if (tagValue != null) {
            hasTagValue = true;
        }

        return hasTagValue;
    }

    public boolean hasRdf() {
        boolean hasRdf = false;

        if (rdf != null) {
            hasRdf = true;
        }

        return hasRdf;
    }

    public boolean hasYaml() {
        boolean hasYaml = false;

        if (yaml != null) {
            hasYaml = true;
        }

        return hasYaml;
    }
}
