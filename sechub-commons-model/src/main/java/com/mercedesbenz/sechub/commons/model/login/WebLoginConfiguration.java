// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.net.URL;
import java.util.Optional;

public class WebLoginConfiguration {

    public static final String PROPERTY_BASIC = "basic";
    public static final String PROPERTY_FORM = "form";
    private URL url;
    Optional<BasicLoginConfiguration> basic = Optional.empty();
    Optional<FormLoginConfiguration> form = Optional.empty();

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Optional<BasicLoginConfiguration> getBasic() {
        return basic;
    }

    public Optional<FormLoginConfiguration> getForm() {
        return form;
    }
}