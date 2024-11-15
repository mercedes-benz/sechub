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
import org.mockito.Mockito;
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

    private static final ZapScriptLoginWebDriverFactory WEB_DRIVER_FACTORY = mock();
    private static final ZapWrapperGroovyScriptExecutor GROOVY_SCRIPT_EXECUTOR = mock();
    private static final ZapScriptLoginSessionGrabber SESSION_GRABBER = mock();

    private static final ClientApiSupport CLIENT_API_SUPPORT = mock();
    private static final FirefoxDriver FIREFOX = mock();

    @BeforeEach
    void beforeEach() {
        Mockito.reset(WEB_DRIVER_FACTORY, GROOVY_SCRIPT_EXECUTOR, SESSION_GRABBER, FIREFOX, CLIENT_API_SUPPORT);
        scriptLoginToTest = new ZapScriptLogin(WEB_DRIVER_FACTORY, GROOVY_SCRIPT_EXECUTOR, SESSION_GRABBER);
    }

    @Test
    void script_login_execution_is_perfomed_as_expected() throws Exception {
        /* prepare */

        ZapScanContext scanContext = createValidZapScanContext();

        when(WEB_DRIVER_FACTORY.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(FIREFOX);
        doNothing().when(GROOVY_SCRIPT_EXECUTOR).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        when(SESSION_GRABBER.extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT)).thenReturn(AUTH_SESSION);

        /* execute */
        scriptLoginToTest.login(scanContext, CLIENT_API_SUPPORT);

        /* test */
        verify(WEB_DRIVER_FACTORY, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(GROOVY_SCRIPT_EXECUTOR, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        verify(SESSION_GRABBER, times(1)).extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT);
        verify(FIREFOX, times(1)).quit();
    }

    @Test
    void script_can_not_be_read_results_in_firefox_closed_and_session_grabber_never_called() throws Exception {
        /* prepare */
        ZapScanContext scanContext = createValidZapScanContext();

        when(WEB_DRIVER_FACTORY.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(FIREFOX);
        doThrow(IOException.class).when(GROOVY_SCRIPT_EXECUTOR).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        when(SESSION_GRABBER.extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT)).thenReturn(AUTH_SESSION);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, CLIENT_API_SUPPORT));

        /* test */
        verify(WEB_DRIVER_FACTORY, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(GROOVY_SCRIPT_EXECUTOR, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        verify(SESSION_GRABBER, never()).extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT);
        verify(FIREFOX, times(1)).quit();
    }

    @Test
    void script_login_execution_fails_results_in_firefox_closed_and_session_grabber_never_called() throws Exception {
        /* prepare */
        ZapScanContext scanContext = createValidZapScanContext();

        when(WEB_DRIVER_FACTORY.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(FIREFOX);
        doThrow(ScriptException.class).when(GROOVY_SCRIPT_EXECUTOR).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        when(SESSION_GRABBER.extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT)).thenReturn(AUTH_SESSION);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, CLIENT_API_SUPPORT));

        /* test */
        verify(WEB_DRIVER_FACTORY, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(GROOVY_SCRIPT_EXECUTOR, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        verify(SESSION_GRABBER, never()).extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT);
        verify(FIREFOX, times(1)).quit();
    }

    @Test
    void session_grabbing_fails_results_in_firefox_closed() throws Exception {
        /* prepare */
        ZapScanContext scanContext = createValidZapScanContext();

        when(WEB_DRIVER_FACTORY.createFirefoxWebdriver(scanContext.getProxyInformation(), true)).thenReturn(FIREFOX);
        doNothing().when(GROOVY_SCRIPT_EXECUTOR).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        doThrow(ClientApiException.class).when(SESSION_GRABBER).extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, CLIENT_API_SUPPORT));

        /* test */
        verify(WEB_DRIVER_FACTORY, times(1)).createFirefoxWebdriver(scanContext.getProxyInformation(), true);
        verify(GROOVY_SCRIPT_EXECUTOR, times(1)).executeScript(scanContext.getGroovyScriptLoginFile(), FIREFOX, scanContext);
        verify(SESSION_GRABBER, times(1)).extractSessionAndPassToZAP(FIREFOX, scanContext.getTargetUrlAsString(), CLIENT_API_SUPPORT);
        verify(FIREFOX, times(1)).quit();
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
