// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.test.ManualTest;
import com.mercedesbenz.sechub.test.TestConstants;
import com.mercedesbenz.sechub.zapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContextFactory;
import com.mercedesbenz.sechub.zapwrapper.config.ZapWrapperContextCreationException;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ScriptLoginResult;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapWrapperGroovyScriptExecutor;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapWrapperGroovyScriptExecutor.ScriptExecutionHook;

class ZapWrapperLoginScriptManualTest implements ManualTest {

    private static final Logger logger = LoggerFactory.getLogger(ZapWrapperLoginScriptManualTest.class);

    private static final String ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_FILE = "zap.loginscript.manualtest.path-to-script-file";
    private static final String ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_PARAMETER_FILE = "zap.loginscript.manualtest.path-to-parameter-file";

    private static File temporarySechubConfigFile;
    private static final TextFileWriter textFileWriter = new TextFileWriter();

    @BeforeAll
    static void beforeAll() {
        temporarySechubConfigFile = new File(ZapWrapperManualTestUtil.getTempFolder(),
                "temp_login_script_sechub-config" + System.currentTimeMillis() + ".json");
    }

    @AfterAll
    static void afterAll() throws IOException {
        // The temporary SecHub configuration file could contain sensitive data so we
        // drop it always
        Files.delete(temporarySechubConfigFile.toPath());
    }

    /**
     *
     * <h2>How to use the test</h2>
     *
     * <pre>
     * You have to set following system properties:
     * - {@value TestConstants#MANUAL_TEST_BY_DEVELOPER} = true
     * - {@value ZapWrapperLoginScriptManualTest#ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_FILE} = path to script file
     * - {@value ZapWrapperLoginScriptManualTest#ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_PARAMETER_FILE} = path to parameters file (in java properties format, will contain variables by key=value lines...)
     * </pre>
     *
     * <h2>Details</h2>
     *
     * This test will
     *
     * <pre>
     * - uses parameter "no-headless" mode -> start will show up web driver
     * - load a custom groovy login script file (configurable) and a file with parameter properties (configurable)
     * - generate a temporary sechub configuration file (will be auto deleted after test)
     * - use  a ZAP wrapper script login executor to start the script and do the login operations.
     * - finally verifies via an execution listeners that after the login has been done, the expected URL (configurable)
     *   has been reached via GET with wanted (configurable) HTTP status code.
     * </pre>
     *
     * <h2>Debugging</h2> You can set a breakpoint into
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
        String pathToScriptFile = System.getProperty(ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_FILE);
        String templateVariablesPath = System.getProperty(ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_PARAMETER_FILE);

        logger.info("Manual login script test started");
        logger.info("--------------------------------");
        logger.info("Attention: As a precondition you must have started a local ZAP on your system!");
        logger.info("> login script file to test from:         '{}'", pathToScriptFile);
        logger.info("> template variables properties file from:'{}'", templateVariablesPath);
        logger.info("> generated sechub configuration at:      '{}'", temporarySechubConfigFile.getAbsolutePath());

        if (pathToScriptFile == null || pathToScriptFile.isEmpty()) {
            throw new IllegalArgumentException("The path to the groovy script file is not defined!\nPlease set the path by jvm parameter: -D"
                    + ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_FILE + "=/location-path/your-script-to-test.groovy");
        }
        File scriptFile = new File(pathToScriptFile);
        if (!scriptFile.isFile()) {
            throw new IllegalArgumentException("Given path points not to an existing file:" + pathToScriptFile);
        }

        if (templateVariablesPath == null || templateVariablesPath.isEmpty()) {
            throw new IllegalArgumentException("The path to the properties file for script execution is not defined!\nPlease set the path by jvm parameter: -D"
                    + ZAP_LOGINSCRIPT_MANUALTEST_PATH_TO_PARAMETER_FILE + "=/location-path/your-parameters.properties");
        }
        File templateVariablesFile = new File(templateVariablesPath);
        if (!templateVariablesFile.isFile()) {
            throw new IllegalArgumentException("Given path points not to an existing file:" + templateVariablesFile);
        }
        Properties templateVariableProperties = new Properties();
        templateVariableProperties.load(new FileInputStream(templateVariablesFile));

        generateSecHubConfigurationFile(temporarySechubConfigFile, templateVariableProperties);

        /* create context */
        ZapScanContext scanContext = createContext(pathToScriptFile);

        /* handle execution setup */
        ZapWrapperGroovyScriptExecutor executor = new ZapWrapperGroovyScriptExecutor();

        LoginVerificationTestScriptExecutionHook verificationTestListener = new LoginVerificationTestScriptExecutionHook();
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

    private ZapScanContext createContext(String pathToScriptFile) throws ZapWrapperContextCreationException {
        ZapScanContextFactory factory = new ZapScanContextFactory();

        CommandLineSettings settings = mock();
        when(settings.getZapHost()).thenReturn("https://not-existing-because-not-needed-inside-this-test.examplle.org");
        when(settings.getJobUUID()).thenReturn(UUID.randomUUID().toString());
        when(settings.getZapPort()).thenReturn(1234);
        when(settings.getZapApiKey()).thenReturn("api-key");
        when(settings.getTargetURL()).thenReturn("https://localhost/"); // hm strange - target url should be inside the sechub configuration - why
                                                                        // setting is here necessary?
        when(settings.getPDSUserMessageFolder()).thenReturn(ZapWrapperManualTestUtil.getUserMessagesFolder().getAbsolutePath());
        when(settings.getPDSEventFolder()).thenReturn(ZapWrapperManualTestUtil.getEventsFolder().getAbsolutePath());
        when(settings.getSecHubConfigFile()).thenReturn(temporarySechubConfigFile);
        when(settings.getGroovyLoginScriptFile()).thenReturn(pathToScriptFile);
        when(settings.isNoHeadless()).thenReturn(true);

        ZapScanContext scanContext = factory.create(settings);
        return scanContext;
    }

    private class LoginVerificationTestScriptExecutionHook implements ScriptExecutionHook {

        private static final Logger logger = LoggerFactory.getLogger(LoginVerificationTestScriptExecutionHook.class);

        private boolean afterScriptExecutedCalled;

        @Override
        public void afterScriptHasBeenExecuted(WebDriver webdriver, ScriptLoginResult loginResult) {
            afterScriptExecutedCalled = true; // for internal health check - we can test listener has been called...

            String url = webdriver.getCurrentUrl();
            logger.info("After login url is: '{}'", url);
            logger.info("Login result says failed={}", loginResult.isLoginFailed());

        }

    }

    private File generateSecHubConfigurationFile(File sechubConfigFile, Properties templateVariableProperties) throws MalformedURLException, IOException {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        WebLoginConfiguration webLoginConfiguration = new WebLoginConfiguration();
        webScan.setLogin(Optional.of(webLoginConfiguration));
        webLoginConfiguration.setUrl(new URL("https://localhost/login"));
        model.setWebScan(webScan);

        TemplateData templateData = new TemplateData();
        templateVariableProperties.forEach((key, value) -> {
            templateData.getVariables().put(key.toString(), value.toString());
        });
        webLoginConfiguration.setTemplateData(templateData);

        String json = JSONConverter.get().toJSON(model, true);
        textFileWriter.writeTextToFile(sechubConfigFile, json, true);

        return sechubConfigFile;
    }

}
