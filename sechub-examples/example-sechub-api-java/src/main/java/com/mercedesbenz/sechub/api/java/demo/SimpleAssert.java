package com.mercedesbenz.sechub.api.java.demo;

import java.util.Objects;

public class SimpleAssert {

    public static void assertEquals(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throw new IllegalStateException("Objects are not equal!" + message);
        }
    }
}
