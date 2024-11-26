// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;

public class ZapScriptLogin {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScriptLogin.class);

    private ZapWrapperGroovyScriptExecutor groovyScriptExecutor;
    private ZapScriptLoginSessionConfigurator sessionConfigurator;

    public ZapScriptLogin() {
        this(new ZapWrapperGroovyScriptExecutor(), new ZapScriptLoginSessionConfigurator());
    }

    ZapScriptLogin(ZapWrapperGroovyScriptExecutor groovyScriptExecutor, ZapScriptLoginSessionConfigurator sessionConfigurator) {
        this.groovyScriptExecutor = groovyScriptExecutor;
        this.sessionConfigurator = sessionConfigurator;
    }

    /**
     * Performs the login by calling the script execution. Afterwards the
     * sessionGrabber will add all necessary session data to ZAP.
     *
     * @param scanContext
     * @param clientApiWrapper
     * @return the name/identifier of the authenticated session inside ZAP
     */
    public String login(ZapScanContext scanContext, ClientApiWrapper clientApiWrapper) {
        File groovyScriptLoginFile = scanContext.getGroovyScriptLoginFile();
        if (groovyScriptLoginFile == null || !groovyScriptLoginFile.isFile()) {
            throw new ZapWrapperRuntimeException(
                    "Expected a groovy script file to perform login, but no script was found. Cannot perform script login without the script file.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }

        LOG.info("Calling groovy script executor to execute login script.");
        ScriptLoginResult loginResult = groovyScriptExecutor.executeScript(groovyScriptLoginFile, scanContext);
        if (loginResult.isLoginFailed()) {
            throw new ZapWrapperRuntimeException("An error happened during script login.", ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        }
        try {
            LOG.info("Calling session grabber to read the HTTP session data and pass them to ZAP.");
            return sessionConfigurator.passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper);
        } catch (ClientApiException e) {
            throw new ZapWrapperRuntimeException("An error happened while grabbing the session data.", e, ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        }
    }

    public void cleanUpScriptLoginData(String targetUrl, ClientApiWrapper clientApiWrapper) throws ClientApiException {
        sessionConfigurator.cleanUpOldSessionDataIfNecessary(targetUrl, clientApiWrapper);
    }

}
