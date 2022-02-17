// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.net.URL;

public abstract class AbstractLoginConfig implements LoginConfig {

    URL loginUrl;

    public URL getLoginURL() {
        return loginUrl;
    }
}
