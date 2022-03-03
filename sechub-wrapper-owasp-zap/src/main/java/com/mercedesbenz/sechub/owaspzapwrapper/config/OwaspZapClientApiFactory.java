// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

public class OwaspZapClientApiFactory {

    public ClientApi create(OwaspZapScanConfiguration scanConfig) throws ClientApiException {
        OwaspZapServerConfiguration serverConfig = scanConfig.getServerConfig();
        String zaproxyHost = serverConfig.getZaproxyHost();
        int zaproxyPort = serverConfig.getZaproxyPort();
        String zaproxyApiKey = serverConfig.getZaproxyApiKey();

        ClientApi clientApi = new ClientApi(zaproxyHost, zaproxyPort, zaproxyApiKey);

        setupBasicConfiguration(clientApi, scanConfig);
        setupAdditonalProxyConfiguration(clientApi, scanConfig);

        return clientApi;
    }

    private void setupBasicConfiguration(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) throws ClientApiException {
        // to ensure parts from previous scan are deleted
        clientApi.core.newSession(scanConfig.getContextName(), "true");
        // setting this value to zero means unlimited
        clientApi.core.setOptionMaximumAlertInstances("0");
    }

    private void setupAdditonalProxyConfiguration(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) throws ClientApiException {
        ProxyInformation proxyInformation = scanConfig.getProxyInformation();
        if (proxyInformation != null) {
            clientApi.core.setOptionProxyChainName(proxyInformation.getHost());
            clientApi.core.setOptionProxyChainPort(proxyInformation.getPort());
            clientApi.core.setOptionUseProxyChain(true);
        } else {
            clientApi.core.setOptionUseProxyChain(false);
        }
    }
}
