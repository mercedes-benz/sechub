// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

public class OwaspZapClientApiFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapClientApiFactory.class);

    public ClientApi create(OwaspZapServerConfiguration serverConfig) {
        LOG.info("Creating Owasp Zap ClientApi.");
        assertValidServerConfig(serverConfig);
        String zaproxyHost = serverConfig.getZaproxyHost();
        int zaproxyPort = serverConfig.getZaproxyPort();
        String zaproxyApiKey = serverConfig.getZaproxyApiKey();

        ClientApi clientApi = new ClientApi(zaproxyHost, zaproxyPort, zaproxyApiKey);

        return clientApi;
    }

    private void assertValidServerConfig(OwaspZapServerConfiguration serverConfig) {
        if (serverConfig == null) {
            throw new ZapWrapperRuntimeException("Owasp Zap server configuration may not be null!", ZapWrapperExitCode.ZAP_CONFIGURATION_INVALID);
        }
        if (serverConfig.getZaproxyHost() == null) {
            throw new ZapWrapperRuntimeException("Owasp Zap host configuration may not be null!", ZapWrapperExitCode.ZAP_CONFIGURATION_INVALID);
        }
        if (serverConfig.getZaproxyPort() <= 0) {
            throw new ZapWrapperRuntimeException("Owasp Zap host configuration ahs to be a valid port number!", ZapWrapperExitCode.ZAP_CONFIGURATION_INVALID);
        }
        if (serverConfig.getZaproxyApiKey() == null) {
            throw new ZapWrapperRuntimeException("Owasp Zap api-key configuration may not be null!", ZapWrapperExitCode.ZAP_CONFIGURATION_INVALID);
        }
    }
}
