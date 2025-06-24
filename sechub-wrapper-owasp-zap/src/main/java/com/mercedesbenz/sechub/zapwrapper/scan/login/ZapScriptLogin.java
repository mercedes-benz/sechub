// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
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
        ScriptLoginResult loginResult = resilientLogin(() -> groovyScriptExecutor.executeScript(groovyScriptLoginFile, scanContext),
                scanContext.getMaximumLoginScriptFailureRetries());
        if (loginResult.isLoginFailed()) {
            String errorMessage = "An error happened during script login. Please verify your credentials are specified correctly.";
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(new SecHubMessage(SecHubMessageType.ERROR, errorMessage));
            throw new ZapWrapperRuntimeException(errorMessage, ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
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

    private ScriptLoginResult resilientLogin(ScriptLoginRunnable loginRunnable, int maxRetries) {
        int loginsDone = 0;
        int maximumAllowedLoginTries = maxRetries + 1; // plus one because this is the "normal" login, without retry

        ScriptLoginResult result = null;

        while (isLoginNecessary(loginsDone, result, maximumAllowedLoginTries)) {

            LOG.info("Start login attempt: {}/{}", loginsDone + 1, maximumAllowedLoginTries);
            result = loginRunnable.login();
            loginsDone++;

            if (result.isLoginFailed()) {
                LOG.warn("Login attempt {} failed!", loginsDone);
            }
        }
        return result;
    }

    private boolean isLoginNecessary(int loginsDone, ScriptLoginResult result, int maximumAllowedLogins) {
        if (result == null) {
            return true;
        }
        if (loginsDone >= maximumAllowedLogins) {
            return false;
        }
        return result.isLoginFailed();
    }

    private interface ScriptLoginRunnable {
        public ScriptLoginResult login();
    }

}
