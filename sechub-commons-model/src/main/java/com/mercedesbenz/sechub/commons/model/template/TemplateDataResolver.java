// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;

public class TemplateDataResolver {

    public TemplateData resolveTemplateData(TemplateType type, SecHubConfigurationModel configuration) {
        if (type == null) {
            return null;
        }
        if (configuration == null) {
            return null;
        }
        switch (type) {
        case WEBSCAN_LOGIN:
            return resolveWebScanLoginTemplateData(configuration);
        default:
            break;
        }
        return null;
    }

    private TemplateData resolveWebScanLoginTemplateData(SecHubConfigurationModel configuration) {
        Optional<SecHubWebScanConfiguration> webScanOpt = configuration.getWebScan();
        if (webScanOpt.isEmpty()) {
            return null;
        }
        SecHubWebScanConfiguration webScan = webScanOpt.get();
        Optional<WebLoginConfiguration> loginOpt = webScan.getLogin();
        if (loginOpt.isEmpty()) {
            return null;
        }
        WebLoginConfiguration login = loginOpt.get();
        return login.getTemplateData();

    }
}
