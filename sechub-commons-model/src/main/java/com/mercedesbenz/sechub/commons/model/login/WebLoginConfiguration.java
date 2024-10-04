// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.net.URL;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebLoginConfiguration {

    public static final String PROPERTY_BASIC = "basic";
    public static final String PROPERTY_FORM = "form";
    public static final String PROPERTY_TOTP = "totp";

    private URL url;
    Optional<BasicLoginConfiguration> basic = Optional.empty();
    Optional<FormLoginConfiguration> form = Optional.empty();

    private WebLoginTOTPConfiguration totp;

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

    public WebLoginTOTPConfiguration getTotp() {
        return totp;
    }

    public void setTotp(WebLoginTOTPConfiguration totp) {
        this.totp = totp;
    }

}