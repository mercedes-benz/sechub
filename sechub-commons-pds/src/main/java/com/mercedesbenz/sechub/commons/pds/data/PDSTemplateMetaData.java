// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds.data;

import java.util.Objects;

import com.mercedesbenz.sechub.commons.model.template.TemplateType;

public class PDSTemplateMetaData {

    private String templateId;
    private TemplateType templateType;
    private PDSAssetData assetData;

    public PDSAssetData getAssetData() {
        return assetData;
    }

    public String getTemplateId() {
        return templateId;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType type) {
        this.templateType = type;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setAssetData(PDSAssetData assetData) {
        this.assetData = assetData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetData, templateId, templateType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PDSTemplateMetaData other = (PDSTemplateMetaData) obj;
        return Objects.equals(assetData, other.assetData) && Objects.equals(templateId, other.templateId) && templateType == other.templateType;
    }

    @Override
    public String toString() {
        return "PDSTemplateMetaData [" + (templateId != null ? "template=" + templateId + ", " : "")
                + (templateType != null ? "type=" + templateType + ", " : "") + (assetData != null ? "assetData=" + assetData : "") + "]";
    }

    public static class PDSAssetData {
        private String assetId;
        private String fileName;
        private String checksum;

        public String getAssetId() {
            return assetId;
        }

        public void setAssetId(String assetId) {
            this.assetId = assetId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(assetId, checksum, fileName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PDSAssetData other = (PDSAssetData) obj;
            return Objects.equals(assetId, other.assetId) && Objects.equals(checksum, other.checksum) && Objects.equals(fileName, other.fileName);
        }

        @Override
        public String toString() {
            return "PDSAssetData [" + (assetId != null ? "assetId=" + assetId + ", " : "") + (fileName != null ? "fileName=" + fileName + ", " : "")
                    + (checksum != null ? "checksum=" + checksum : "") + "]";
        }

    }
}
