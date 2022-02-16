// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.util;

import java.util.Collection;

/**
 * Some simple assert methods
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSAssert {

    private PDSAssert() {

    }

    /**
     * Throws an illegal argument exception when given object is <code>null</code>
     *
     * @param obj
     * @param message
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an illegal argument exception when given string is <code>null</code>
     * or empty
     *
     * @param string
     * @param message
     */
    public static void notEmpty(String string, String message) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an illegal argument exception when given collection is
     * <code>null</code> or empty
     *
     * @param string
     * @param message
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an illegal argument exception when given array is <code>null</code> or
     * empty
     *
     * @param string
     * @param message
     */
    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an illegal argument exception when given string has length greater
     * than given max
     *
     * @param string
     * @param max
     */
    public static void maxLength(String string, int max) {
        if (string == null || string.isEmpty()) {
            return;
        }
        int length = string.length();
        if (length > max) {
            throw new IllegalArgumentException("Maximum of " + max + " chars allowd, but got:" + length);
        }
    }

}
