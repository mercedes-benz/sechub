// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
public class AssetFileData {

    private String checksum;

    private String fileName;

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checksum, fileName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AssetFileData other = (AssetFileData) obj;
        return Objects.equals(checksum, other.checksum) && Objects.equals(fileName, other.fileName);
    }

    @Override
    public String toString() {
        return "AssetFileInformation [" + (checksum != null ? "checksum=" + checksum + ", " : "") + (fileName != null ? "fileName=" + fileName : "") + "]";
    }

}
