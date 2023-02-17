// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import java.util.List;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;

public class ImportParameter {
    private String productId;
    private String importData;
    private String importId;
    private List<SecHubMessage> productMessages;

    public static ImportParamBuilder builder() {
        return new ImportParamBuilder();
    }

    public String getProductId() {
        return productId;
    }

    public String getImportData() {
        return importData;
    }

    public String getImportId() {
        return importId;
    }

    public List<SecHubMessage> getProductMessages() {
        return productMessages;
    }

    public ScanType getScanType() {
        ScanType scanType = ScanType.UNKNOWN;

        if (productId == "NESSUS" || productId == "PDS_INFRASCAN") {
            scanType = ScanType.INFRA_SCAN;
        } else if (productId == "CHECKMARX" || productId == "PDS_CODESCAN") {
            scanType = ScanType.CODE_SCAN;
        } else if (productId == "NETSPARKER" || productId == "PDS_WEBSCAN") {
            scanType = ScanType.WEB_SCAN;
        } else if (productId == "PDS_LICENSESCAN") {
            scanType = ScanType.LICENSE_SCAN;
        } else if (productId == "PDS_SECRETSCAN") {
            scanType = ScanType.SECRET_SCAN;
        } else if (productId == "PDS_ANALYTICS") {
            scanType = ScanType.ANALYTICS;
        }

        return scanType;
    }

    /**
     * Builder for input parameters
     *
     * @author Albert Tregnaghi
     *
     */
    public static class ImportParamBuilder {

        private ImportParameter param;

        private ImportParamBuilder() {
            clear();
        }

        private void clear() {
            this.param = new ImportParameter();
        }

        public ImportParamBuilder productId(String productId) {
            param.productId = productId;
            return this;
        }

        public ImportParamBuilder importData(String importData) {
            param.importData = importData;
            return this;
        }

        public ImportParamBuilder importId(String importId) {
            param.importId = importId;
            return this;
        }

        public ImportParamBuilder importProductMessages(List<SecHubMessage> messages) {
            param.productMessages = messages;
            return this;
        }

        public ImportParameter build() {
            ImportParameter result = param;
            clear();
            return result;
        }

    }
}