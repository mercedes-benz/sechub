// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

public class FilenameVariantConverter {

    public static final String DEFAULT_IDENTIFIER = "-variant_";
    private String variantIdentifier;

    /**
     * Uses {@link #DEFAULT_IDENTIFIER} as variant identifier
     */
    public FilenameVariantConverter() {
        this(null);
    }

    /**
     * Creates a convert with given identifier.
     *
     * @param varIdentifier when null default identifier {@link #DEFAULT_IDENTIFIER}
     *                      is used.
     */
    public FilenameVariantConverter(String varIdentifier) {
        if (varIdentifier == null) {
            varIdentifier = DEFAULT_IDENTIFIER;
        }
        this.variantIdentifier = varIdentifier;
    }

    /**
     * Resolves variant definition from given file name (e.g.
     * test1234-variant_test.html will be resolved to "test" variant when default
     * identifier {@link #DEFAULT_IDENTIFIER} is used)
     *
     * @param fileName
     * @return
     */
    public String getVariantFromFilename(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.indexOf(variantIdentifier);
        if (index == -1) {
            return "";
        }
        String subString = fileName.substring(index + variantIdentifier.length());
        index = subString.indexOf('.');
        if (index == -1) {
            return "";
        }
        return subString.substring(0, index);
    }

    /**
     * Builds a variant file name - e.g. given filename 'test1234.html' and variant
     * 'test' will be transformed to "test1234-variant_test.html" when default
     * identifier {@link #DEFAULT_IDENTIFIER} is used.
     *
     * @param fileName
     * @param variant
     * @return
     */
    public String toVariantFileName(String fileName, String variant) {
        if (variant == null || variant.isEmpty()) {
            return fileName;
        }
        if (fileName == null) {
            throw new IllegalArgumentException("filename may not be null!");
        }
        int index = fileName.indexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException("no '.' found, so not able to create variant file name!");
        }
        String postfix = fileName.substring(index);
        String prefix = fileName.substring(0, index);

        return prefix + variantIdentifier + variant + postfix;
    }

    public String getFilenameWithoutVariant(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = fileName.indexOf(variantIdentifier);
        if (index == -1) {
            return fileName;
        }
        String before = fileName.substring(0, index);
        String subString = fileName.substring(index + variantIdentifier.length());
        index = subString.indexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException("no '.' found, but this is not allowed!");
        }
        String ending = subString.substring(index);
        return before + ending;
    }

}
