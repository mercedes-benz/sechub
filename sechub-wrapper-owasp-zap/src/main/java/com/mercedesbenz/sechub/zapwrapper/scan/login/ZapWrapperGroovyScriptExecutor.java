// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptBindingKeys.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginTOTPConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.util.TOTPGenerator;

public class ZapWrapperGroovyScriptExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ZapWrapperGroovyScriptExecutor.class);
    private static final Logger scriptLogger = LoggerFactory.getLogger("groovy-script-logger");

    private static final int WEBDRIVER_TIMEOUT_PER_STEP_IN_SECONDS = 30;

    private static final String LOCAL_STORAGE = "localStorage";
    private static final String SESSION_STORAGE = "sessionStorage";

    private ZapScriptLoginWebDriverFactory webDriverFactory;

    private int webdriverTimeoutInSeconds;

    private List<ScriptExecutionHook> hooks = new ArrayList<>(0);

    public ZapWrapperGroovyScriptExecutor() {
        this(new ZapScriptLoginWebDriverFactory(), WEBDRIVER_TIMEOUT_PER_STEP_IN_SECONDS);
    }

    /**
     * Constructor for mocking approaches
     *
     * @param webDriverFactory          factory
     * @param webdriverTimeoutInSeconds timeout
     */
    ZapWrapperGroovyScriptExecutor(ZapScriptLoginWebDriverFactory webDriverFactory, int webdriverTimeoutInSeconds) {
        this.webDriverFactory = webDriverFactory;
        this.webdriverTimeoutInSeconds = webdriverTimeoutInSeconds;
    }

    public List<ScriptExecutionHook> getHooks() {
        return hooks;
    }

    public ScriptLoginResult executeScript(File scriptFile, ZapScanContext scanContext) {
        boolean headless = !scanContext.isNoHeadless();

        FirefoxDriver firefox = webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), scanContext.getPacFilePath(), headless);
        WebDriverWait wait = new WebDriverWait(firefox, Duration.ofSeconds(webdriverTimeoutInSeconds));

        ScriptEngine scriptEngine = new GroovyScriptEngineFactory().getScriptEngine();

        LOG.info("Create bindings for groovy script.");
        Bindings bindings = createBindings(scanContext, scriptEngine, firefox, wait);

        ScriptLoginResult loginResult = new ScriptLoginResult();
        try {
            String script = Files.readString(scriptFile.toPath());
            LOG.info("Execute groovy login script.");
            scriptEngine.eval(script, bindings);

            LOG.info("Execution successful, preparing login result with session data.");
            loginResult.setSessionCookies(firefox.manage().getCookies());
            loginResult.setSessionStorage(retrieveStorage(firefox, SESSION_STORAGE));
            loginResult.setLocalStorage(retrieveStorage(firefox, LOCAL_STORAGE));
        } catch (IOException | ScriptException e) {
            LOG.error("An error happened while executing the script file.", e);
            loginResult.setLoginFailed(true);
        } catch (UserInfoScriptException e) {
            LOG.error("An error, which is reported to the user, happened while executing the script file.", e);
            loginResult.setLoginFailed(true);
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(new SecHubMessage(SecHubMessageType.ERROR, e.getMessage()));
        } finally {
            try {
                hooks.forEach((hook) -> hook.afterScriptHasBeenExecuted(firefox, wait, loginResult));
            } catch (Exception e) {
                LOG.error("Hook handling for afterLoginScriptHasBeenExecuted(..) has failed!", e);
            } finally {
                firefox.quit();
            }

        }
        return loginResult;
    }

    private Bindings createBindings(ZapScanContext scanContext, ScriptEngine scriptEngine, FirefoxDriver firefox, WebDriverWait wait) {
        SecHubWebScanConfiguration secHubWebScanConfiguration = scanContext.getSecHubWebScanConfiguration();
        WebLoginConfiguration webLoginConfiguration = secHubWebScanConfiguration.getLogin().get();

        WebLoginTOTPConfiguration totpConfiguration = webLoginConfiguration.getTotp();
        TOTPGenerator totpGenerator = null;
        if (totpConfiguration != null) {
            try {
                LOG.info("Creating TOTP generator for login.");
                totpGenerator = new TOTPGenerator(totpConfiguration);
            } catch (IllegalArgumentException e) {
                LOG.error("Could not create TOTP generator for login", e);
                SecHubMessage productMessage = new SecHubMessage(SecHubMessageType.ERROR, "Please check the TOTP configuration because: " + e.getMessage());
                scanContext.getZapProductMessageHelper().writeSingleProductMessage(productMessage);
            }
        }

        /* json data from web configuration - fetch variables */
        Map<String, String> templateVariables = scanContext.getTemplateVariables();

        Bindings bindings = scriptEngine.createBindings();

        /* Custom bindings by user */
        bindings.putAll(templateVariables);

        /* Web driver */
        bindings.put(WEBDRIVER, firefox);
        bindings.put(WEBDRIVER_WAIT, wait);

        /* Script engine */
        bindings.put(JAVASCRIPT_EXECUTOR, firefox);

        /* SecHub configuration */
        bindings.put(SECHUB_WEBSCAN_CONFIG, secHubWebScanConfiguration);
        if (webLoginConfiguration.getUrl() != null) {
            bindings.put(LOGIN_URL, webLoginConfiguration.getUrl().toString());
        } else {
            // if no dedicated login URL is set we assume an automated redirect
            bindings.put(LOGIN_URL, scanContext.getTargetUrlAsString());
        }
        bindings.put(TARGET_URL, scanContext.getTargetUrlAsString());

        /* Additional */
        bindings.put(TOTP_GENERATOR, totpGenerator);
        bindings.put(LOGGER, scriptLogger);

        return bindings;
    }

    private Map<String, String> retrieveStorage(JavascriptExecutor jsExecutor, String storageType) {
        String script = """
                let items = {};
                for (let i = 0; i < %s.length; i++) {
                  let key = %s.key(i);
                  items[key] = %s.getItem(key);
                }
                return items;
                """.formatted(storageType, storageType, storageType);

        @SuppressWarnings("unchecked")
        Map<String, String> storage = (Map<String, String>) jsExecutor.executeScript(script);
        return storage;
    }

    public interface ScriptExecutionHook {

        /**
         * Script has been executed and result is available. Given web driver is still
         * active at this moment and can be used inside hook
         *
         * @param webdriver     web driver used for login
         * @param webDriverWait web driver wait object used for login
         * @param loginResult   the result
         */
        public void afterScriptHasBeenExecuted(WebDriver webdriver, WebDriverWait webDriverWait, ScriptLoginResult loginResult);
    }

}
