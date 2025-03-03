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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import com.mercedesbenz.sechub.test.ManualTest;
import com.mercedesbenz.sechub.test.TestConstants;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContextFactory;
import com.mercedesbenz.sechub.zapwrapper.config.ZapWrapperContextCreationException;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ScriptLoginResult;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapWrapperGroovyScriptExecutor;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapWrapperGroovyScriptExecutor.ScriptExecutionHook;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableToSystemPropertyConverter;

class ZapWrapperLoginScriptManualTest implements ManualTest {

    private static final String DEFAULT_PARAMETERS_PROPERTIES_PATH = "./src/test/resources/manual-test/example-parameters.properties";

    private static final String TOTP_SEED = "test.login.totp.seed";
    private static final String LOGIN_SCRIPT_PATH = "test.login.script.path";
    private static final String LOGIN_URL = "test.login.url";
    private static final String TARGET_URL = "test.target.url";
    private static final String LOGIN_VERFICATION_URL = "test.login.verification.url";
    private static final String LOGIN_VERIFICATION_CSS_SELECTOR = "test.login.verification.css-selector";
    private static final String LOGIN_VERIFICATION_EXPECTED_VALUE = "test.login.verification.expected-value";

    private static final Logger logger = LoggerFactory.getLogger(ZapWrapperLoginScriptManualTest.class);

    private static final String SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE = "loginscript.manualtest.configfile";

    @BeforeAll
    static void beforeAll() {

    }

    @AfterAll
    static void afterAll() throws IOException {
    }

    private String verificationCssSelector;

    private String verificationExpectedValue;

    /**
     *
     * <h3>How to use the test</h3>
     *
     * You have to set following system properties:
     * <ul>
     * {@value TestConstants#MANUAL_TEST_BY_DEVELOPER} = true</li>
     * <li>{@value ZapWrapperLoginScriptManualTest#SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE}
     * = path to parameters file (see below for format). If not set, a fallback
     * default path {@value #DEFAULT_PARAMETERS_PROPERTIES_PATH} will be used. The
     * example configuration is a good point to start, every parameter is explained
     * there. Please copy the example file to a custom local location, configure it
     * and use it by setting the system property accordingly when executing the
     * test.</li>
     * </ul>
     * Start as normal junit5 test inside your IDE. It is not necessary to have a
     * running ZAP instance- the test works standalone!
     *
     * <h4>Parameter properties file format</h3> You can use a custom properties
     * file: It has to contain variables by "key=value" lines. <br>
     * The values inside the properties file can be values or reference environment
     * variables - e.g. "password=env:SECRET_USER_PASSWORD" <br>
     * Please look at
     * {@link SystemEnvironmentVariableSupport#getValueOrVariableContent(String)}
     * for details or look into the fallback parameter file at
     * {@value #DEFAULT_PARAMETERS_PROPERTIES_PATH} <br>
     * <br>
     * Some keys are reserved:
     *
     * {@value #TOTP_SEED} : if this is set , the login configuration will
     * automatically use the seed.
     *
     *
     * <h3>Details</h2>
     *
     * This test will
     *
     * <ul>
     * <li>uses parameter "no-headless" mode -> start will show up web driver</li>
     * <li>load a custom groovy login script file (configurable) and a file with
     * parameter properties (configurable)</li>
     * <li>generate a temporary sechub configuration in memory
     * <li>use a ZAP wrapper script login executor to start the script and do the
     * login operations.</li>
     * <li>finally verifies via an execution listeners that after the login has been
     * done, the expected URL (configurable</li> has been reached via GET with
     * wanted (configurable) HTTP status code.
     * </ul>
     *
     * <h2>Debugging</h3> You can set a breakpoint into
     * {@link LoginVerificationTestScriptExecutionHook#afterScriptExecutedCalled} -
     * and start the test in debug mode. This will keep the web driver window open
     * and you can see what the result of the login script was on browser UI.
     *
     * @throws Exception
     */
    @Test
    void manual_start_login_script_with_login_success() throws Exception {
        /* ------- */
        /* prepare */
        /* ------- */
        String testConfigurationFilePath = System.getProperty(SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE);
        if (testConfigurationFilePath == null) {
            logger.info("************************");
            logger.info("****    ATTENTION   ****");
            logger.info("************************");
            logger.info("No test configuration file defined. Will use fallback default. \nTo use your own configuration start test with jvm parameter: -D"
                    + SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE + "=your-location/your-configuration.properties");
            logger.info("************************");
            testConfigurationFilePath = DEFAULT_PARAMETERS_PROPERTIES_PATH;
        }

        logger.info("Manual login script test started");
        logger.info("--------------------------------");
        logger.info("> using configuration file from:'{}'", testConfigurationFilePath);

        if (testConfigurationFilePath == null || testConfigurationFilePath.isEmpty()) {
            throw new IllegalArgumentException("The path to the properties file for script execution is not defined!\nPlease set the path by jvm parameter: -D"
                    + SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE + "=/location-path/your-parameters.properties");
        }
        File templateVariablesFile = new File(testConfigurationFilePath);
        if (!templateVariablesFile.isFile()) {
            throw new IllegalArgumentException("Given path points not to an existing file:" + templateVariablesFile);
        }
        Properties templateVariableProperties = new Properties();
        templateVariableProperties.load(new FileInputStream(templateVariablesFile));

        Map<String, String> map = createVariablesMapWithValuesOrEnvironmentContent(testConfigurationFilePath, templateVariableProperties);
        initSecHubConfiguration(map);
        String pathToScriptFile = map.get(LOGIN_SCRIPT_PATH);
        logger.info("From test configuration file:" + testConfigurationFilePath);
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
        when(configuration.getPDSUserMessageFolder()).thenReturn(ZapWrapperManualTestUtil.getUserMessagesFolder().getAbsolutePath());
        when(configuration.getPDSEventFolder()).thenReturn(ZapWrapperManualTestUtil.getEventsFolder().getAbsolutePath());
        when(configuration.getGroovyLoginScriptFile()).thenReturn(pathToScriptFile);
        when(configuration.isNoHeadless()).thenReturn(true);

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
            if (key.startsWith("variables.")) {
                String variableName = key.substring("variables.".length());
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

    private class LoginVerificationTestScriptExecutionHook implements ScriptExecutionHook {

        private static final Logger logger = LoggerFactory.getLogger(LoginVerificationTestScriptExecutionHook.class);

        private boolean afterScriptExecutedCalled;

        private String verificationUrl;

        private LoginVerificationTestScriptExecutionHook(String verifcationUrl) {
            this.verificationUrl = verifcationUrl;
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
