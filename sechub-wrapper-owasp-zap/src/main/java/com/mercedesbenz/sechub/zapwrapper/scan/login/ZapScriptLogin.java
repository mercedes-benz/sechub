// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiSupport;

public class ZapScriptLogin {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScriptLogin.class);

    private ZapScriptLoginWebDriverFactory webDriverFactory;
    private ZapWrapperGroovyScriptExecutor groovyScriptExecutor;
    private ZapScriptLoginSessionGrabber sessionGrabber;

    public ZapScriptLogin(ZapScriptLoginWebDriverFactory webDriverFactory, ZapWrapperGroovyScriptExecutor groovyScriptExecutor,
            ZapScriptLoginSessionGrabber sessionGrabber) {
        this.webDriverFactory = webDriverFactory;
        this.groovyScriptExecutor = groovyScriptExecutor;
        this.sessionGrabber = sessionGrabber;
    }

    /**
     * Performs the login by calling the script execution. Afterwards the
     * sessionGrabber will add all necessary session data to ZAP.
     *
     * @param scanContext
     * @param clientApiSupport
     * @return the name/identifier of the authenticated session inside ZAP
     */
    public String login(ZapScanContext scanContext, ClientApiSupport clientApiSupport) {
        File groovyScriptLoginFile = scanContext.getGroovyScriptLoginFile();
        if (groovyScriptLoginFile == null || !groovyScriptLoginFile.isFile()) {
            throw new ZapWrapperRuntimeException(
                    "Expected a groovy script file to perform login, but no script was found. Cannot perform script login without the script file.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        LOG.info("Creating selenium web driver.");
        FirefoxDriver firefox = webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), true);

        try {
            LOG.info("Calling groovy script executor to execute login script.");
            groovyScriptExecutor.executeScript(groovyScriptLoginFile, firefox, scanContext);

            LOG.info("Calling session grabber to read the HTTP session data and pass them to ZAP.");
            return sessionGrabber.extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport);
        } catch (IOException e) {
            throw new ZapWrapperRuntimeException(e.getMessage(), e, ZapWrapperExitCode.IO_ERROR);
        } catch (ScriptException e) {
            throw new ZapWrapperRuntimeException(e.getMessage(), e, ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        } catch (ClientApiException e) {
            throw new ZapWrapperRuntimeException(e.getMessage(), e, ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        } finally {
            firefox.quit();
        }
    }

    public void cleanUpScriptLoginData(String targetUrl, ClientApiSupport clientApiSupport) {
        sessionGrabber.cleanUpOldSessionDataIfNecessary(targetUrl, clientApiSupport);

    }

}
