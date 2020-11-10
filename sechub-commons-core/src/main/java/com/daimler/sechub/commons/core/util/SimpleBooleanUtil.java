// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.core.util;

public class SimpleBooleanUtil {

    public static boolean isFalseOrNull(Boolean b) {
        return ! isTrue(b);
    }
    public static boolean isTrue(Boolean b) {
        return b!=null && b.booleanValue();
    }
}
