// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class PDSProductSetup {
    private static final boolean DEFAULT_EXTRACT_UPLOADS = true;

    private static final long DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCRESULT = 120; // 2 hours

    private boolean extractUploads = DEFAULT_EXTRACT_UPLOADS;

    private String id;

    private ScanType scanType;

    private String path;

    private long minutesToWaitForProductResult = DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCRESULT;

    /**
     * The description. Will be available at admin UI at configuration time.
     * Contains hints about usage (e.g. which variable must be set etc) - but is
     * optional
     */
    private String description;

    private PDSProdutParameterSetup parameters = new PDSProdutParameterSetup();

    public PDSProdutParameterSetup getParameters() {
        return parameters;
    }

    public void setParameters(PDSProdutParameterSetup parameters) {
        this.parameters = parameters;
    }

    /**
     * Represents the product identifier suitable for sechub
     *
     * @return
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

    public boolean isExtractUploads() {
        return extractUploads;
    }

    public void setExtractUploads(boolean extractUploads) {
        this.extractUploads = extractUploads;
    }

    public long getMinutesToWaitForProductResult() {
        if (minutesToWaitForProductResult < 1) {
            minutesToWaitForProductResult = DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCRESULT;
        }
        return minutesToWaitForProductResult;
    }

    public void setMinutesToWaitForProductResult(long minutesToWaitForProductResult) {
        this.minutesToWaitForProductResult = minutesToWaitForProductResult;
    }

}
