// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;

class ZapScriptLoginTest {

    private static final String AUTH_SESSION = "auth-session";

    private ZapScriptLogin scriptLoginToTest;

    private ZapWrapperGroovyScriptExecutor groovyScriptExecutor;
    private ZapScriptLoginSessionConfigurator sessionConfigurator;

    private ClientApiWrapper clientApiWrapper;

    @BeforeEach
    void beforeEach() {
        groovyScriptExecutor = mock();
        sessionConfigurator = mock();
        clientApiWrapper = mock();

        scriptLoginToTest = new ZapScriptLogin(groovyScriptExecutor, sessionConfigurator);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void when_script_is_NOT_failing_only_one_execution_and_no_failure(Integer retries) throws Exception {
        /* prepare */
        ScriptLoginResult loginResult1 = mock();
        when(loginResult1.isLoginFailed()).thenReturn(false);

        ZapScanContext scanContext = createValidZapScanContext();
        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult1);

        /* execute + test */
        scriptLoginToTest.login(scanContext, clientApiWrapper);

        verify(groovyScriptExecutor, times(1)).executeScript(any(File.class), eq(scanContext));

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3 })
    void when_script_is_failing_and_retries_are_done_but_also_fail_then_script_fails_with_wrapper_runtime_exception(Integer retries) throws Exception {
        /* prepare */
        ScriptLoginResult loginResult1 = mock();
        when(loginResult1.isLoginFailed()).thenReturn(true);

        ScriptLoginResult loginResult2 = mock();
        when(loginResult2.isLoginFailed()).thenReturn(true);

        ScriptLoginResult loginResult3 = mock();
        when(loginResult3.isLoginFailed()).thenReturn(true);

        ScriptLoginResult loginResult4 = mock();
        when(loginResult4.isLoginFailed()).thenReturn(true);

        ZapScanContext scanContext = createValidZapScanContext(retries);
        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult1, loginResult2, loginResult3,
                loginResult4);

        /* execute + test */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiWrapper));

        assertTrue(exception.getMessage().contains("An error happened during script login"));
        verify(groovyScriptExecutor, times(retries + 1)).executeScript(any(File.class), eq(scanContext));

    }

    @Test
    void when_script_is_failing_first_time_but_not_second_time_no_wrapper_runtime_exception() throws Exception {
        /* prepare */
        ScriptLoginResult loginResult1 = mock();
        when(loginResult1.isLoginFailed()).thenReturn(true);

        ScriptLoginResult loginResult2 = mock();
        when(loginResult2.isLoginFailed()).thenReturn(false);

        ZapScanContext scanContext = createValidZapScanContext(1);
        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult1, loginResult2);

        /* execute */
        scriptLoginToTest.login(scanContext, clientApiWrapper);

        /* test */
        verify(groovyScriptExecutor, times(2)).executeScript(any(File.class), eq(scanContext));
    }

    @Test
    void script_login_execution_is_perfomed_as_expected() throws Exception {
        /* prepare */
        ScriptLoginResult loginResult = new ScriptLoginResult();
        ZapScanContext scanContext = createValidZapScanContext();

        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult);
        when(sessionConfigurator.passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper)).thenReturn(AUTH_SESSION);

        /* execute */
        scriptLoginToTest.login(scanContext, clientApiWrapper);

        /* test */
        verify(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), scanContext);
        verify(sessionConfigurator).passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    @Test
    void script_can_not_be_read_results_in_firefox_closed_and_session_configurator_never_called() throws Exception {
        /* prepare */
        ScriptLoginResult loginResult = new ScriptLoginResult();
        loginResult.setLoginFailed(true);
        ZapScanContext scanContext = createValidZapScanContext();

        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult);
        when(sessionConfigurator.passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper)).thenReturn(AUTH_SESSION);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiWrapper));

        /* test */
        verify(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), scanContext);
        verify(sessionConfigurator, never()).passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    @Test
    void script_login_execution_fails_results_in_firefox_closed_and_session_configurator_never_called() throws Exception {
        /* prepare */
        ScriptLoginResult loginResult = new ScriptLoginResult();
        loginResult.setLoginFailed(true);
        ZapScanContext scanContext = createValidZapScanContext();

        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult);
        when(sessionConfigurator.passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper)).thenReturn(AUTH_SESSION);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiWrapper));

        /* test */
        verify(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), scanContext);
        verify(sessionConfigurator, never()).passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    @Test
    void session_configurator_fails_results_in_excpetion_thrown() throws Exception {
        /* prepare */
        ScriptLoginResult loginResult = new ScriptLoginResult();
        ZapScanContext scanContext = createValidZapScanContext();

        when(groovyScriptExecutor.executeScript(scanContext.getGroovyScriptLoginFile(), scanContext)).thenReturn(loginResult);
        doThrow(ClientApiException.class).when(sessionConfigurator).passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper);

        /* execute */
        assertThrows(ZapWrapperRuntimeException.class, () -> scriptLoginToTest.login(scanContext, clientApiWrapper));

        /* test */
        verify(groovyScriptExecutor).executeScript(scanContext.getGroovyScriptLoginFile(), scanContext);
        verify(sessionConfigurator).passSessionDataToZAP(loginResult, scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    private ZapScanContext createValidZapScanContext() throws MalformedURLException, URISyntaxException {
        return createValidZapScanContext(0);
    }

    private ZapScanContext createValidZapScanContext(int maxAllowedLoginScriptRetries) throws MalformedURLException, URISyntaxException {
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
                .setMaxGroovyScriptLoginFailureRetries(maxAllowedLoginScriptRetries)
                .setZapProductMessageHelper(mock())
                .build();
        /* @formatter:on */
    }

}
