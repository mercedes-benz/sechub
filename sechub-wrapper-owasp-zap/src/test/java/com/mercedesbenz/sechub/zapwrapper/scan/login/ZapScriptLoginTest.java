// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiSupport;

class ZapScriptLoginTest {

    private static final String AUTH_SESSION = "auth-session";

    private ZapScriptLogin scriptLoginToTest;

    private ZapScriptLoginWebDriverFactory webDriverFactory = mock();
    private ZapWrapperGroovyScriptExecutor groovyScriptExecutor = mock();
    private ZapScriptLoginSessionGrabber sessionGrabber = mock();

    private ClientApiSupport clientApiSupport = mock();
    private FirefoxDriver firefox = mock();

    @BeforeEach
    void beforeEach() {
        webDriverFactory = mock();
        groovyScriptExecutor = mock();
        sessionGrabber = mock();
        clientApiSupport = mock();
        firefox = mock();

        scriptLoginToTest = new ZapScriptLogin(webDriverFactory, groovyScriptExecutor, sessionGrabber);
    }

    @Test
    void script_login_execution_is_perfomed_as_expected() throws Exception {
        /* prepare */

        ZapScanContext scanContext = createValidZapScanContext();

        when(webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(firefox);
        doNothing().when(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        when(sessionGrabber.extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport)).thenReturn(AUTH_SESSION);

        /* execute */
        scriptLoginToTest.login(scanContext, clientApiSupport);

        /* test */
        verify(webDriverFactory, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(groovyScriptExecutor, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        verify(sessionGrabber, times(1)).extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport);
        verify(firefox, times(1)).quit();
    }

    @Test
    void script_can_not_be_read_results_in_firefox_closed_and_session_grabber_never_called() throws Exception {
        /* prepare */
        ZapScanContext scanContext = createValidZapScanContext();

        when(webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(firefox);
        doThrow(IOException.class).when(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        when(sessionGrabber.extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport)).thenReturn(AUTH_SESSION);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiSupport));

        /* test */
        verify(webDriverFactory, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(groovyScriptExecutor, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        verify(sessionGrabber, never()).extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport);
        verify(firefox, times(1)).quit();
    }

    @Test
    void script_login_execution_fails_results_in_firefox_closed_and_session_grabber_never_called() throws Exception {
        /* prepare */
        ZapScanContext scanContext = createValidZapScanContext();

        when(webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(firefox);
        doThrow(ScriptException.class).when(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        when(sessionGrabber.extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport)).thenReturn(AUTH_SESSION);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiSupport));

        /* test */
        verify(webDriverFactory, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(groovyScriptExecutor, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        verify(sessionGrabber, never()).extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport);
        verify(firefox, times(1)).quit();
    }

    @Test
    void session_grabbing_fails_results_in_firefox_closed() throws Exception {
        /* prepare */
        ZapScanContext scanContext = createValidZapScanContext();

        when(webDriverFactory.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(firefox);
        doNothing().when(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        doThrow(ClientApiException.class).when(sessionGrabber).extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiSupport));

        /* test */
        verify(webDriverFactory, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(groovyScriptExecutor, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), firefox, scanContext);
        verify(sessionGrabber, times(1)).extractSessionAndPassToZAP(firefox, scanContext.getTargetUrlAsString(), clientApiSupport);
        verify(firefox, times(1)).quit();
    }

    private ZapScanContext createValidZapScanContext() throws MalformedURLException, URISyntaxException {
        URL targetUrl = new URL("http://example.com");

        WebLoginConfiguration login = new WebLoginConfiguration();
        login.setUrl(new URL("http://example.com/login"));

        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.setUrl(targetUrl.toURI());
        webScanConfig.setLogin(Optional.of(login));
        File scriptFile = new File("src/test/resources/login-script-examples/test-script.groovy");
        /* @formatter:off */
        return ZapScanContext.builder()
                .setSecHubWebScanConfiguration(webScanConfig)
                .setGroovyScriptLoginFile(scriptFile)
                .setTargetUrl(targetUrl)
                .build();
        /* @formatter:on */
    }

}
