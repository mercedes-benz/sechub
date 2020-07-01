package com.daimler.sechub.pds.config;

public class PDSProductSetup {

    private String id;

    private PDSScanType scanType;

    private String path;

    /**
     * The description. Will be available at admin UI at configuration time.
     * Contains hints about usage (e.g. which variable must be set etc) - but is
     * optional
     */
    private String description;

    /**
     * Represents the product identifier suitable for sechub
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PDSScanType getScanType() {
        return scanType;
    }

    public void setScanType(PDSScanType scanType) {
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
