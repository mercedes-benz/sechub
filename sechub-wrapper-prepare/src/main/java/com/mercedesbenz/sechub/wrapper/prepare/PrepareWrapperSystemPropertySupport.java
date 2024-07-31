// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import org.springframework.stereotype.Component;

@Component
public class PrepareWrapperSystemPropertySupport {
    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }
}
