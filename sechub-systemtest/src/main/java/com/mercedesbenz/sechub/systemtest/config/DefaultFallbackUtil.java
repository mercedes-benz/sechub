// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultFallbackUtil {

    public static URL convertToURL(DefaultFallback fallback) {
        String urlAsString = fallback.getValue();
        try {
            return new URL(urlAsString);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url inside a fallback:" + urlAsString, e);
        }
    }

    public static boolean convertToBoolean(DefaultFallback fallback) {
        return Boolean.parseBoolean(fallback.getValue());
    }
}
