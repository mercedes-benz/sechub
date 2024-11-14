// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds.data;

import java.util.Objects;

import com.mercedesbenz.sechub.commons.model.template.TemplateType;

public class PDSTemplateMetaData {

    private String template;
    private TemplateType type;
    private PDSAssetData assetData;

    public PDSAssetData getAssetData() {
        return assetData;
    }

    public String getTemplate() {
        return template;
    }

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    public void setTemplate(String templateId) {
        this.template = templateId;
    }

    public void setAssetData(PDSAssetData assetData) {
        this.assetData = assetData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetData, template, type);
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
        return Objects.equals(assetData, other.assetData) && Objects.equals(template, other.template) && type == other.type;
    }

    @Override
    public String toString() {
        return "PDSTemplateMetaData [" + (template != null ? "template=" + template + ", " : "") + (type != null ? "type=" + type + ", " : "")
                + (assetData != null ? "assetData=" + assetData : "") + "]";
    }

    public static class PDSAssetData {
        private String asset;
        private String file;
        private String checksum;

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(asset, checksum, file);
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
            return Objects.equals(asset, other.asset) && Objects.equals(checksum, other.checksum) && Objects.equals(file, other.file);
        }

        @Override
        public String toString() {
            return "PDSAssetData [" + (asset != null ? "asset=" + asset + ", " : "") + (file != null ? "file=" + file + ", " : "")
                    + (checksum != null ? "checksum=" + checksum : "") + "]";
        }

    }
}
