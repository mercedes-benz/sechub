// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.ScanType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PDSProductSetup {

    private String id;

    private ScanType scanType;

    private String path;

    /**
     * The description. Will be available at admin UI at configuration time.
     * Contains hints about usage (e.g. which variable must be set etc) - but is
     * optional
     */
    private String description;

    private PDSProductParameterSetup parameters = new PDSProductParameterSetup();

    public PDSProductParameterSetup getParameters() {
        return parameters;
    }

    public void setParameters(PDSProductParameterSetup parameters) {
        this.parameters = parameters;
    }

    /**
     * Represents the product identifier suitable for sechub. May not be null - this
     * will be ensured by validation.
     *
     * @return product identifier, never <code>null</code>
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
