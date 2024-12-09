// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptBindingKeys.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginTOTPConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapTemplateDataVariableKeys;
import com.mercedesbenz.sechub.zapwrapper.util.TOTPGenerator;
import com.mercedesbenz.sechub.zapwrapper.util.ZapWrapperStringDecoder;

public class ZapWrapperGroovyScriptExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ZapWrapperGroovyScriptExecutor.class);

    private static final int WEBDRIVER_TIMEOUT_PER_STEP_IN_SECONDS = 30;

    private static final String LOCAL_STORAGE = "localStorage";
    private static final String SESSION_STORAGE = "sessionStorage";

    private ZapScriptLoginWebDriverFactory webDriverFactory;

    private int webdriverTimeoutInSeconds;

    public ZapWrapperGroovyScriptExecutor() {
        this(new ZapScriptLoginWebDriverFactory(), WEBDRIVER_TIMEOUT_PER_STEP_IN_SECONDS);
    }

    ZapWrapperGroovyScriptExecutor(ZapScriptLoginWebDriverFactory webDriverFactory, int webdriverTimeoutInSeconds) {
        this.webDriverFactory = webDriverFactory;
        this.webdriverTimeoutInSeconds = webdriverTimeoutInSeconds;
    }

    public ScriptLoginResult executeScript(File scriptFile, ZapScanContext scanContext) {

        FirefoxDriver firefox = webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), true);
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
        } finally {
            firefox.quit();
        }
        return loginResult;
    }

    private Bindings createBindings(ZapScanContext scanContext, ScriptEngine scriptEngine, FirefoxDriver firefox, WebDriverWait wait) {
        SecHubWebScanConfiguration secHubWebScanConfiguration = scanContext.getSecHubWebScanConfiguration();
        WebLoginConfiguration webLoginConfiguration = secHubWebScanConfiguration.getLogin().get();

        WebLoginTOTPConfiguration totp = webLoginConfiguration.getTotp();
        TOTPGenerator totpGenerator = null;
        if (totp != null) {
            LOG.info("Trying to decode TOTP seed if necessary.");
            ZapWrapperStringDecoder zapWrapperStringDecoder = new ZapWrapperStringDecoder();
            byte[] decodedSeedBytes = zapWrapperStringDecoder.decodeIfNecessary(totp.getSeed(), totp.getEncodingType());
            String decodedSeed = new String(decodedSeedBytes, StandardCharsets.UTF_8);

            LOG.info("Setting up TOTP generator for login.");
            totpGenerator = new TOTPGenerator(decodedSeed, totp.getTokenLength(), totp.getHashAlgorithm(), totp.getValidityInSeconds());
        }

        Map<String, String> templateVariables = scanContext.getTemplateVariables();

        Bindings bindings = scriptEngine.createBindings();
        bindings.put(FIREFOX_WEBDRIVER_KEY, firefox);
        bindings.put(FIREFOX_WEBDRIVER_WAIT_KEY, wait);
        bindings.put(JAVASCRIPTEXECUTOR_KEY, firefox);
        bindings.put(SECHUB_WEBSCAN_CONFIG_KEY, secHubWebScanConfiguration);
        bindings.put(TOTP_GENERATOR_KEY, totpGenerator);

        bindings.put(USER_KEY, templateVariables.get(ZapTemplateDataVariableKeys.USERNAME_KEY));
        bindings.put(PASSWORD_KEY, templateVariables.get(ZapTemplateDataVariableKeys.PASSWORD_KEY));
        if (webLoginConfiguration.getUrl() != null) {
            bindings.put(LOGIN_URL_KEY, webLoginConfiguration.getUrl().toString());
        } else {
            // if no dedicated login URL is set we assume an automated redirect
            bindings.put(LOGIN_URL_KEY, scanContext.getTargetUrlAsString());
        }
        bindings.put(TARGET_URL_KEY, scanContext.getTargetUrlAsString());

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
}
