// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import java.util.List;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

public class ImportParameter {
    private String productId;
    private String importData;
    private String importId;
    private List<SecHubMessage> productMessages;
    private boolean canceled;

    public static ImportParamBuilder builder() {
        return new ImportParamBuilder();
    }
    
    public boolean isCanceled() {
        return canceled;
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
        ProductIdentifier productIdentifier = ProductIdentifier.fromString(productId);

        return productIdentifier.getType();
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
        
        public ImportParamBuilder canceled(boolean canceled) {
            param.canceled=canceled;
            return this;
        }

        public ImportParameter build() {
            ImportParameter result = param;
            clear();
            return result;
        }


    }
}