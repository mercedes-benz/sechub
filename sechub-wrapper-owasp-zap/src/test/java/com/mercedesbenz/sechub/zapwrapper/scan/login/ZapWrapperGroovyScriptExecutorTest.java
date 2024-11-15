// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;

class ZapWrapperGroovyScriptExecutorTest {

    private ZapWrapperGroovyScriptExecutor scriptExecutorToTest;

    @BeforeEach
    void beforeEach() {
        scriptExecutorToTest = new ZapWrapperGroovyScriptExecutor();
    }

    @Test
    void throws_io_exception_when_script_cannot_be_read() {
        /* prepare */
        File scriptFile = new File("not-existing.groovy");

        /* execute + test */
        assertThrows(IOException.class, () -> scriptExecutorToTest.executeScript(scriptFile, null, null));
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

        /* execute + test */
        assertThrows(ScriptException.class, () -> scriptExecutorToTest.executeScript(scriptFile, null, zapScanContext));
    }

    @Test
    void valid_script_is_executed_as_expected() throws Exception {
        /* prepare */
        File scriptFile = new File("src/test/resources/login-script-examples/test-script.groovy");

        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.setUrl(URI.create("http://example.com"));

        WebLoginConfiguration login = new WebLoginConfiguration();
        login.setUrl(new URL("http://example.com/login"));
        webScanConfig.setLogin(Optional.of(login));

        ZapScanContext zapScanContext = ZapScanContext.builder().setSecHubWebScanConfiguration(webScanConfig).setTargetUrl(webScanConfig.getUrl().toURL())
                .build();

        /* execute + test */
        assertDoesNotThrow(() -> scriptExecutorToTest.executeScript(scriptFile, null, zapScanContext));
    }

}
