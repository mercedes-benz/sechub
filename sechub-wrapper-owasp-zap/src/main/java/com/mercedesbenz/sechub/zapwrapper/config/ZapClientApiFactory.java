// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.scan.ClientApiFacade;

public class ZapClientApiFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ZapClientApiFactory.class);

    public ClientApiFacade create(ZapServerConfiguration serverConfig) {
        LOG.info("Creating Zap ClientApi.");
        assertValidServerConfig(serverConfig);
        String zaproxyHost = serverConfig.getZaproxyHost();
        int zaproxyPort = serverConfig.getZaproxyPort();
        String zaproxyApiKey = serverConfig.getZaproxyApiKey();

        ClientApi clientApi = new ClientApi(zaproxyHost, zaproxyPort, zaproxyApiKey);

        return new ClientApiFacade(clientApi);
    }

    private void assertValidServerConfig(ZapServerConfiguration serverConfig) {
        if (serverConfig == null) {
            throw new ZapWrapperRuntimeException("Zap server configuration may not be null!", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (serverConfig.getZaproxyHost() == null) {
            throw new ZapWrapperRuntimeException("Zap host configuration may not be null!", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (serverConfig.getZaproxyPort() <= 0) {
            throw new ZapWrapperRuntimeException("Zap host configuration ahs to be a valid port number!", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (serverConfig.getZaproxyApiKey() == null) {
            throw new ZapWrapperRuntimeException("Zap api-key configuration may not be null!", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
    }
}
