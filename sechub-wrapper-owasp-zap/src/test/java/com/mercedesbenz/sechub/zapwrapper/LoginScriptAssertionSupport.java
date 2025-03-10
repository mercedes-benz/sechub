// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironment;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginTOTPConfiguration;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContextFactory;
import com.mercedesbenz.sechub.zapwrapper.config.ZapWrapperContextCreationException;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ScriptLoginResult;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapWrapperGroovyScriptExecutor;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapWrapperGroovyScriptExecutor.ScriptExecutionHook;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableToSystemPropertyConverter;

public class LoginScriptAssertionSupport {

    public static final String PREFIX_VARIABLES = "variables.";
    public static final String TOTP_SEED = "test.login.totp.seed";
    public static final String LOGIN_SCRIPT_PATH = "test.login.script.path";
    public static final String LOGIN_URL = "test.login.url";
    public static final String TARGET_URL = "test.target.url";
    public static final String LOGIN_VERFICATION_URL = "test.login.verification.url";
    public static final String LOGIN_VERIFICATION_CSS_SELECTOR = "test.login.verification.css-selector";
    public static final String LOGIN_VERIFICATION_EXPECTED_VALUE = "test.login.verification.expected-value";

    private static final Logger logger = LoggerFactory.getLogger(LoginScriptAssertionSupport.class);

    private String verificationCssSelector;

    private String verificationExpectedValue;
    private String userMessagesFolder;
    private String eventsFolder;
    private boolean noHeadless;

    /**
     * A support class to assert login script behavior.
     *
     * @param userMessagesFolder the local user messages folder which can be used by
     *                           scripts (for testing)
     * @param eventsFolder       the local events folder which can be used by
     *                           scripts (for testing)
     * @param noHeadless         when <code>true</code> the user interface will be
     *                           visible, when <code>false</code> there will be no
     *                           visible browser available
     */
    public LoginScriptAssertionSupport(String userMessagesFolder, String eventsFolder, boolean noHeadless) {
        this.userMessagesFolder = userMessagesFolder;
        this.eventsFolder = eventsFolder;
        this.noHeadless = noHeadless;
    }

    /**
     *
     * Assert login script execution works as expected without using ZAP, means
     * standalone. The expectations and paths etc. are defined inside a properties
     * file.
     *
     * <h3>Properties file format</h3> The properties is just a typical java
     * properties file, with some necessary keys/naming conventions. The values
     * inside the properties file can either be values or reference environment
     * variables - e.g. "password=env:SECRET_USER_PASSWORD" <br>
     * Please look at
     * {@link SystemEnvironmentVariableSupport#getValueOrVariableContent(String)}
     * for details or look into the fallback parameter file at
     * {@value #DEFAULT_PARAMETERS_PROPERTIES_PATH} <br>
     * <br>
     * Some keys are reserved:
     * <ul>
     * <li>{@value #TOTP_SEED} : if this is set , the login configuration will
     * automatically use the seed.</li>
     * <li>{@value #PREFIX_VARIABLES} is a prefix to represents template data
     * variables. The variable name will renamed and runtime. For example: the
     * content of 'variables.username' will be available inside script as binding
     * 'username'</li>
     * <li>{@value #LOGIN_SCRIPT_PATH} : Mandatory, represents the path to the login
     * script which shall be asserted</li>
     * <li>{@value #LOGIN_URL} : Mandatory, represents the login url</li>
     * <li>{@value #TARGET_URL} : Mandatory, represents the target url</li>
     * <li>{@value #LOGIN_VERFICATION_URL} : Optional. If given, the url will be
     * loaded after script login has been done and via css selector a value will be
     * retrieved and asserted that it is as expected. This ensures, that the script
     * login has been really successful</li>
     * <li>{@value #LOGIN_VERIFICATION_CSS_SELECTOR} : Optional. Used when
     * verfication URL is set</li>
     * <li>{@value #LOGIN_VERIFICATION_EXPECTED_VALUE} : Optional. Used when
     * verfication URL is set</li>
     *
     * </ul>
     *
     * <h3>Details</h2>
     *
     * The assertion will
     * <ul>
     * <li>set parameter "no-headless" mode as defined in field -> start will show
     * up web driver UI or not</li>
     * <li>load assertion configuration properties</li>
     * <li>load a custom groovy login script file (defined in assertion properties
     * file)</li>
     * <li>generate a temporary SecHub configuration in memory
     * <li>use a ZAP wrapper script login executor to start the script and do the
     * login operations.</li>
     * <li>finally verifies via an execution listeners that after the login has been
     * done, the expected URL can be opened and contains a value fetchable by CSS
     * selector</li>
     * </ul>
     *
     * <h2>Debugging</h3> You can set a breakpoint into
     * {@link LoginVerificationTestScriptExecutionHook#afterScriptHasBeenExecuted(org.openqa.selenium.WebDriver, org.openqa.selenium.support.ui.WebDriverWait, com.mercedesbenz.sechub.zapwrapper.scan.login.ScriptLoginResult)}
     * and start the test in debug mode. This will keep the web driver window open
     * and you can see what the result of the login script was on browser UI.
     *
     *
     * @param propertiesFilePath path to properties file with setup information for
     *                           assertion
     * @throws Exception
     */
    public void assertConfiguredLoginScriptCanLogin(String propertiesFilePath) throws Exception {
        /* ------- */
        /* prepare */
        /* ------- */
        if (propertiesFilePath == null) {
            throw new IllegalArgumentException("configuration file path may not be null!");
        }

        logger.info("> using configuration file from:'{}'", propertiesFilePath);

        if (propertiesFilePath == null || propertiesFilePath.isEmpty()) {
            throw new IllegalArgumentException("The path to the properties file for script execution is not defined!");
        }
        File templateVariablesFile = new File(propertiesFilePath);
        if (!templateVariablesFile.isFile()) {
            throw new IllegalArgumentException("Given path points not to an existing file:" + templateVariablesFile);
        }
        Properties templateVariableProperties = new Properties();
        templateVariableProperties.load(new FileInputStream(templateVariablesFile));

        Map<String, String> map = createVariablesMapWithValuesOrEnvironmentContent(propertiesFilePath, templateVariableProperties);
        initSecHubConfiguration(map);
        String pathToScriptFile = map.get(LOGIN_SCRIPT_PATH);
        logger.info("From test configuration file:" + propertiesFilePath);
        logger.info("> login script file to test from:         '{}'", pathToScriptFile);
        if (pathToScriptFile == null || pathToScriptFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "The path to the groovy script file is not defined!\nPlease set the path inside test configuration file with key:" + LOGIN_SCRIPT_PATH);
        }
        File scriptFile = new File(pathToScriptFile);
        if (!scriptFile.isFile()) {
            throw new IllegalArgumentException("Given path points not to an existing file:" + pathToScriptFile);
        }

        String verificationUrl = map.get(LOGIN_VERFICATION_URL);
        if (verificationUrl == null || verificationUrl.isEmpty()) {
            logger.info("The parameter: " + LOGIN_VERFICATION_URL + " is not defined, verification that login script  be defined but wasn't");
            verificationUrl = null;
        }
        if (verificationUrl != null) {
            verificationCssSelector = map.get(LOGIN_VERIFICATION_CSS_SELECTOR);
            if (verificationCssSelector == null || verificationCssSelector.isEmpty()) {
                throw new IllegalArgumentException("Verification URL is set, but " + LOGIN_VERIFICATION_CSS_SELECTOR + " is not defined in parameters!");
            }
            verificationExpectedValue = map.get(LOGIN_VERIFICATION_EXPECTED_VALUE);
            if (verificationExpectedValue == null || verificationExpectedValue.isEmpty()) {
                throw new IllegalArgumentException("Verification URL is set, but " + LOGIN_VERIFICATION_EXPECTED_VALUE + " is not defined in parameters!");
            }
        }

        /* create context */
        ZapScanContext scanContext = createContext(pathToScriptFile, map);

        /* handle execution setup */
        ZapWrapperGroovyScriptExecutor executor = new ZapWrapperGroovyScriptExecutor();

        LoginVerificationTestScriptExecutionHook verificationTestListener = new LoginVerificationTestScriptExecutionHook(verificationUrl);
        executor.getHooks().add(verificationTestListener);

        /* ------- */
        /* execute */
        /* ------- */
        ScriptLoginResult result = executor.executeScript(scriptFile, scanContext);

        /* ---- */
        /* test */
        /* ---- */
        assertTrue(verificationTestListener.afterScriptExecutedCalled);
        if (result.isLoginFailed()) {
            fail("Login failed");
        }
    }

    private Map<String, String> createVariablesMapWithValuesOrEnvironmentContent(String templateVariablesPath, Properties templateVariableProperties) {
        Map<String, String> map = new TreeMap<>();
        SystemEnvironmentVariableSupport support = new SystemEnvironmentVariableSupport(new SystemEnvironment());
        for (String key : templateVariableProperties.stringPropertyNames()) {
            String value = templateVariableProperties.getProperty(key);

            String valueOrVariableContent = support.getValueOrVariableContent(value);
            if (value.startsWith("env:") && (valueOrVariableContent == null || valueOrVariableContent.isBlank())) {
                logger.warn("The value for key '" + key + "' refers to a variable " + value
                        + " which has not been defined! Please define the variable before calling the test.");
            }
            map.put(key, valueOrVariableContent);
        }
        support.getValueOrVariableContent(templateVariablesPath);
        return map;
    }

    private ZapScanContext createContext(String pathToScriptFile, Map<String, String> map) throws ZapWrapperContextCreationException {
        ZapScanContextFactory factory = new ZapScanContextFactory();

        ZapWrapperConfiguration configuration = mock();
        when(configuration.getZapHost()).thenReturn("https://not-existing-because-not-needed-inside-this-test.examplle.org");
        when(configuration.getJobUUID()).thenReturn(UUID.randomUUID().toString());
        when(configuration.getZapPort()).thenReturn(1234);
        when(configuration.getZapApiKey()).thenReturn("api-key");
        String targetUrl = fetchTargetUrl(map);
        when(configuration.getTargetURL()).thenReturn(targetUrl);
        when(configuration.getPDSUserMessageFolder()).thenReturn(userMessagesFolder);
        when(configuration.getPDSEventFolder()).thenReturn(eventsFolder);
        when(configuration.getGroovyLoginScriptFile()).thenReturn(pathToScriptFile);
        when(configuration.isNoHeadless()).thenReturn(noHeadless);

        ZapScanContext scanContext = factory.create(configuration);
        return scanContext;
    }

    private void initSecHubConfiguration(Map<String, String> map) throws MalformedURLException, IOException {

        EnvironmentVariableToSystemPropertyConverter converter = new EnvironmentVariableToSystemPropertyConverter();

        String seed = map.remove(TOTP_SEED);
        boolean seedFound = seed != null && !seed.isBlank();
        logger.info("Initialize sechub configuration: used seed found={}, used variables={}", seedFound, map.keySet());

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        WebLoginConfiguration webLoginConfiguration = new WebLoginConfiguration();
        webScan.setLogin(Optional.of(webLoginConfiguration));
        String loginUrlString = fetchLoginUrl(map);
        webLoginConfiguration.setUrl(new URL(loginUrlString));
        if (seedFound) {
            WebLoginTOTPConfiguration totp = new WebLoginTOTPConfiguration();
            totp.setSeed(seed);

            webLoginConfiguration.setTotp(totp);
        }
        model.setWebScan(webScan);

        TemplateData templateData = new TemplateData();
        map.forEach((key, value) -> {
            if (key.startsWith(PREFIX_VARIABLES)) {
                String variableName = key.substring(PREFIX_VARIABLES.length());
                templateData.getVariables().put(variableName, value);
            }
        });
        webLoginConfiguration.setTemplateData(templateData);

        String json = JSONConverter.get().toJSON(model, true);
        System.setProperty(converter.convertEnvironmentVariableToSystemPropertyKey(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION), json);
    }

    private String fetchLoginUrl(Map<String, String> map) {
        String loginUrlString = map.get(LOGIN_URL);
        if (loginUrlString == null || loginUrlString.isEmpty()) {
            throw new IllegalArgumentException("The parameter: " + LOGIN_URL + " must be defined but wasn't");
        }
        return loginUrlString;
    }

    private String fetchTargetUrl(Map<String, String> map) {
        String loginUrlString = map.get(TARGET_URL);
        if (loginUrlString == null || loginUrlString.isEmpty()) {
            throw new IllegalArgumentException("The parameter: " + TARGET_URL + " must be defined but wasn't");
        }
        return loginUrlString;
    }

    public class LoginVerificationTestScriptExecutionHook implements ScriptExecutionHook {

        private static final Logger logger = LoggerFactory.getLogger(LoginVerificationTestScriptExecutionHook.class);

        private boolean afterScriptExecutedCalled;

        private String verificationUrl;

        private LoginVerificationTestScriptExecutionHook(String verificationUrl) {
            this.verificationUrl = verificationUrl;
        }

        @Override
        public void afterScriptHasBeenExecuted(WebDriver webdriver, WebDriverWait webdriverWait, ScriptLoginResult loginResult) {
            afterScriptExecutedCalled = true; // for internal health check - we can test listener has been called...

            String currentUrl = webdriver.getCurrentUrl();
            logger.info("After login url is: '{}'", currentUrl);
            logger.info("Login result says failed={}", loginResult.isLoginFailed());
            if (verificationUrl == null) {
                logger.info("- No verification defined, skip verification step");
            } else {
                logger.info("- Start verification by opening url: {}", verificationUrl);
                webdriver.get(verificationUrl);
                String text = webdriverWait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(verificationCssSelector))).getText();
                logger.info("Login verfication fetched value '' from css-selector: '{}'", text, verificationCssSelector);
                assertEquals(verificationExpectedValue, text);
            }

        }
    }

}
