// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptBindingKeys.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginTOTPConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.util.TOTPGenerator;
import com.mercedesbenz.sechub.zapwrapper.util.ZapWrapperStringDecoder;

public class ZapWrapperGroovyScriptExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ZapWrapperGroovyScriptExecutor.class);

    private static final long WEBDRIVER_TIMEOUT_PER_STEP_IN_SECONDS = 30;

    public void executeScript(File scriptFile, FirefoxDriver firefox, ZapScanContext scanContext) throws IOException, ScriptException {

        String script = Files.readString(scriptFile.toPath());
        ScriptEngine scriptEngine = new GroovyScriptEngineFactory().getScriptEngine();

        LOG.info("Create bindings for groovy script.");
        Bindings bindings = createBindings(scanContext, scriptEngine, firefox);

        LOG.info("Execute groovy login script.");
        scriptEngine.eval(script, bindings);
    }

    private Bindings createBindings(ZapScanContext scanContext, ScriptEngine scriptEngine, FirefoxDriver firefox) {
        // TODO 2024-11-21 jan: use templates structure from sechub webscan config
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

        // TODO 2024-11-21 jan: read the username and password from templateData as soon
        // as it is
        // implemented
        String user = "DUMMY";
        String password = "DUMMY";

        WebDriverWait wait = new WebDriverWait(firefox, Duration.ofSeconds(WEBDRIVER_TIMEOUT_PER_STEP_IN_SECONDS));

        Bindings bindings = scriptEngine.createBindings();
        bindings.put(FIREFOX_WEBDRIVER_KEY, firefox);
        bindings.put(FIREFOX_WEBDRIVER_WAIT_KEY, wait);
        bindings.put(JAVASCRIPTEXECUTOR_KEY, firefox);
        bindings.put(SECHUB_WEBSCAN_CONFIG_KEY, secHubWebScanConfiguration);
        bindings.put(TOTP_GENERATOR_KEY, totpGenerator);

        bindings.put(USER_KEY, user);
        bindings.put(PASSWORD_KEY, password);
        bindings.put(LOGIN_URL_KEY, webLoginConfiguration.getUrl().toString());
        bindings.put(TARGET_URL_KEY, scanContext.getTargetUrlAsString());

        return bindings;
    }
}
