// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static void assertEquals(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throw new IllegalStateException(message+" - Objects are not equal!");
        }
    }

    public static void waitMilliseconds(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.error("Was not able to wait for {} milliseconds", millis, e);
            Thread.currentThread().interrupt();
        }

    }

    public static void assumeEquals(Object obj1, Object obj2, String message) {
        assertEquals(obj1, obj2, message);
        /* if no errror... */
        logSuccess(message);
    }

    public static void logTitle(String title) {
        LOG.info("");
        LOG.info("   {}  ", title);
        LOG.info("*".repeat(title.length() + 6));
    }

    public static void logSuccess(String text) {
        LOG.info("  ✅ {}", text);
    }

    public static void logWarn(String text) {
        LOG.warn("  ⚠️ {}", text);
    }

    public static void logError(String text, Throwable t) {
        LOG.error("  🔥 {}", text, t);
    }

}
