// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;

class ZapWrapperGroovyScriptExecutorTest {

    private ZapWrapperGroovyScriptExecutor scriptExecutorToTest;

    private ZapScriptLoginWebDriverFactory webDriverFactory;
    private FirefoxDriver firefox;
    private Options options;

    @BeforeEach
    void beforeEach() {
        webDriverFactory = mock();
        firefox = mock();
        options = mock();

        scriptExecutorToTest = new ZapWrapperGroovyScriptExecutor(webDriverFactory, 0);

        when(webDriverFactory.createFirefoxWebdriver(any(), any(), anyBoolean())).thenReturn(firefox);
        when(firefox.manage()).thenReturn(options);
        when(options.getCookies()).thenReturn(Collections.emptySet());
    }

    @Test
    void throws_io_exception_when_script_cannot_be_read() throws Exception {
        /* prepare */
        File scriptFile = new File("not-existing.groovy");

        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.setUrl(URI.create("http://example.com"));

        WebLoginConfiguration login = new WebLoginConfiguration();
        login.setUrl(new URL("http://example.com/login"));
        webScanConfig.setLogin(Optional.of(login));

        ZapScanContext zapScanContext = ZapScanContext.builder().setSecHubWebScanConfiguration(webScanConfig).setTargetUrl(webScanConfig.getUrl().toURL())
                .build();

        /* execute */
        ScriptLoginResult loginResult = scriptExecutorToTest.executeScript(scriptFile, zapScanContext);

        /* test */
        assertTrue(loginResult.isLoginFailed());
    }

    @Test
    void throws_script_exception_when_script_contains_errors() throws Exception {
        /* prepare */
        File scriptFile = new File("src/test/resources/login-script-examples/invalid-script.groovy");

        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.setUrl(URI.create("http://example.com"));

        WebLoginConfiguration login = new WebLoginConfiguration();
        login.setUrl(new URL("http://example.com/login"));
        webScanConfig.setLogin(Optional.of(login));

        ZapScanContext zapScanContext = ZapScanContext.builder().setSecHubWebScanConfiguration(webScanConfig).setTargetUrl(webScanConfig.getUrl().toURL())
                .build();

        /* execute */
        ScriptLoginResult loginResult = scriptExecutorToTest.executeScript(scriptFile, zapScanContext);

        /* test */
        assertTrue(loginResult.isLoginFailed());
    }

    @Test
    void valid_script_is_executed_as_expected() throws Exception {
        /* prepare */
        File scriptFile = new File("src/test/resources/login-script-examples/test-script.groovy");

        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.setUrl(URI.create("http://example.com"));

        WebLoginConfiguration login = new WebLoginConfiguration();
        login.setUrl(new URL("http://example.com/login"));
        TemplateData templateData = new TemplateData();
        templateData.getVariables().putAll(Map.of("custom-username", "user1", "custom-password", "pwd1"));
        login.setTemplateData(templateData);
        webScanConfig.setLogin(Optional.of(login));

        ZapScanContext zapScanContext = ZapScanContext.builder().setSecHubWebScanConfiguration(webScanConfig).setTargetUrl(webScanConfig.getUrl().toURL())
                .build();

        /* execute */
        ScriptLoginResult loginResult = scriptExecutorToTest.executeScript(scriptFile, zapScanContext);

        /* test */
        assertFalse(loginResult.isLoginFailed());
    }

    @Test
    void throws_user_info_script_exception_results_in_result_failed() throws Exception {
        /* prepare */
        File scriptFile = new File("src/test/resources/login-script-examples/throw-user-info-script-exception.groovy");

        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.setUrl(URI.create("http://example.com"));

        WebLoginConfiguration login = new WebLoginConfiguration();
        login.setUrl(new URL("http://example.com/login"));
        webScanConfig.setLogin(Optional.of(login));

        ZapScanContext zapScanContext = ZapScanContext.builder().setSecHubWebScanConfiguration(webScanConfig).setTargetUrl(webScanConfig.getUrl().toURL())
                .build();

        /* execute */
        ScriptLoginResult loginResult = scriptExecutorToTest.executeScript(scriptFile, zapScanContext);

        /* test */
        assertTrue(loginResult.isLoginFailed());
    }

}
