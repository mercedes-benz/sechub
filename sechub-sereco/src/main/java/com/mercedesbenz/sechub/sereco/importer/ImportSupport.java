// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.sereco.ImportParameter;

/**
 * Support to check if an importer can handle JSON or XML content. It simply
 * checks if content contains one wellknown part
 *
 * @author Albert Tregnaghi
 *
 */
public class ImportSupport {

    private static final char BOM = 65279;

    private List<String> productIds;

    private String contentIdentifier;

    private boolean checkXML;

    private boolean checkJSON;

    /**
     * Create support
     *
     * @param productIdParts - the parts to identify the productId
     */
    private ImportSupport() {
    }

    public static ImportSupportBuilder builder() {
        return new ImportSupportBuilder();
    }

    public static class ImportSupportBuilder {
        List<String> productIds = new ArrayList<>();
        String contentIdentifier;

        private boolean checkXML;

        private boolean checkJSON;

        private ImportSupportBuilder() {

        }

        public ImportSupportBuilder productId(String productId) {
            this.productIds.clear();
            if (productId != null) {
                this.productIds.add(productId.toLowerCase());
            }
            return this;
        }

        public ImportSupportBuilder productIds(String... productIds) {
            this.productIds.clear();
            if (productIds != null) {
                for (String productId : productIds) {
                    if (productId != null) {
                        this.productIds.add(productId.toLowerCase());
                    }
                }
            }
            return this;
        }

        /**
         * A simple way to identify something inside a text which is identifying the
         * product importer can import this. Be aware! This is the most simple approach
         * and works only when you can 100% identify this by this way!<br>
         * <br>
         * For example: when you have to importers being able to import
         *
         * <pre>
         * &lt;fancyReport&gt;
         * </pre>
         *
         * but one is for version 1.0 the other is for version 2.0... The importer would
         * say "I am able to import" but if will fail because wrong version... <br>
         * <b>In those cases you MUST implement logic inside the importer itself and use
         * this import support only as a first check</b>
         *
         * @param contentIdentifier
         * @return
         */
        public ImportSupportBuilder contentIdentifiedBy(String contentIdentifier) {
            this.contentIdentifier = contentIdentifier;
            return this;
        }

        public ImportSupportBuilder mustBeXML() {
            this.checkXML = true;
            return this;
        }

        public ImportSupportBuilder mustBeJSON() {
            this.checkJSON = true;
            return this;
        }

        public ImportSupport build() {
            validate();
            ImportSupport support = new ImportSupport();

            support.checkJSON = checkJSON;
            support.checkXML = checkXML;
            support.contentIdentifier = contentIdentifier;
            support.productIds = productIds;

            return support;
        }

        private void validate() {
            if (checkJSON && checkXML) {
                throw new IllegalStateException("You have defined xml and JSON - this makes no sense!");
            }
        }
    }

    /**
     * Checks if the given import parameter can be imported or not
     *
     * @param parameter
     * @return
     */
    public ProductImportAbility isAbleToImport(ImportParameter parameter) {
        if (!isProductIdentified(parameter.getProductId())) {
            return ProductImportAbility.NOT_ABLE_TO_IMPORT;
        }
        if (isEmpty(parameter.getImportData())) {
            return ProductImportAbility.PRODUCT_FAILED_OR_CANCELED;
        }
        if (!isContentIdentified(parameter.getImportData())) {
            return ProductImportAbility.NOT_ABLE_TO_IMPORT;
        }
        if (checkXML && !isXML(parameter.getImportData())) {
            return ProductImportAbility.NOT_ABLE_TO_IMPORT;
        }
        if (checkJSON && !isJSON(parameter.getImportData())) {
            return ProductImportAbility.NOT_ABLE_TO_IMPORT;
        }
        return ProductImportAbility.ABLE_TO_IMPORT;

    }

    private boolean isProductIdentified(String productId) {
        if (this.productIds.isEmpty()) {
            return true; // when nothing defined set we accept all
        }
        if (productId == null) {
            return false;
        }
        String lowerCased = productId.trim().toLowerCase();
        return productIds.contains(lowerCased);
    }

    private boolean isContentIdentified(String content) {
        if (content == null) {
            return false;
        }
        if (contentIdentifier == null) {
            return true; /* always.. */
        }
        return content.contains(contentIdentifier);
    }

    boolean isEmpty(String data) {
        if (data == null) {
            return true;
        }
        if (data.length() == 0) {
            return true;
        }
        return false;
    }

    boolean isJSON(String data) {
        return createSubStringWithoutBOMandLowercased(data, 1).startsWith("{");
    }

    boolean isXML(String data) {
        return createSubStringWithoutBOMandLowercased(data, 5).startsWith("<?xml");
    }

    private String createSubStringWithoutBOMandLowercased(String origin, int size) {
        if (origin == null) {
            return "";
        }
        int pos = 0;
        int length = origin.length();
        StringBuilder sb = new StringBuilder();

        while (sb.length() <= size && pos < length) {
            char c = origin.charAt(pos++);
            if (c != BOM) {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();

    }

}
