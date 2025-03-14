// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserIdTransformationSupport {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserIdTransformationSupport.class);

    public String transformBrowserIdWhenNoHeadless(boolean noHeadless, String browserId) {
        if (!noHeadless) {
            return browserId;
        }
        if (browserId == null) {
            return null;
        }

        ZAPAcceptedBrowserId found = null;
        for (ZAPAcceptedBrowserId acceptedBrowserId : ZAPAcceptedBrowserId.values()) {
            if (browserId.equals(acceptedBrowserId.getBrowserId())) {
                found = acceptedBrowserId;
            }
        }
        if (found == null) {
            LOG.error("Browser id: '{}' is not supported!");
            return browserId;
        }

        ZAPAcceptedBrowserId result = null;

        switch (found) {
        case CHROME_HEADLESS:
            result = ZAPAcceptedBrowserId.CHROME;
            break;
        case FIREFOX_HEADLESS:
            result = ZAPAcceptedBrowserId.FIREFOX;
            break;
        default:
            break;
        }

        if (result == null) {
            result = found;
            LOG.warn("Cannot determine a non-headless variant for '{}' - will use '{}'", result, found);
        }

        return result.getBrowserId();
    }

}
