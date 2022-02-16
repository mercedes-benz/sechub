// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.test;

public class TestUtils {

    public static boolean equals(Object object1, Object object2) {
        if (object1 == null && object2 == null) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }

        return object1.equals(object2);
    }

    public static boolean contains(String string, String partWhichShallBeContained) {
        if (equals(string, partWhichShallBeContained)) {
            return true;
        }
        if (string == null || partWhichShallBeContained == null) {
            return false;
        }
        return string.contains(partWhichShallBeContained);
    }
}
